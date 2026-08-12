package com.ruoyi.mall.review.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.exception.ServiceException;

/** 评价图片使用若依资源目录，返回可被现有资源映射读取的 URL。 */
@Service
public class MallProductReviewImageStorageService
{
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;
    private static final long MAX_PIXELS = 25_000_000L;
    private static final int MAX_DIMENSION = 8192;
    private static final Set<String> EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    public String store(Long memberId, MultipartFile file)
    {
        if (memberId == null || memberId <= 0) throw new ServiceException("会员身份无效");
        if (file == null || file.isEmpty()) throw new ServiceException("请选择评价图片");
        if (file.getSize() > MAX_FILE_SIZE) throw new ServiceException("评价图片不能超过 5MB");
        String filename = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String extension = extension(filename);
        String contentType = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!EXTENSIONS.contains(extension) || !CONTENT_TYPES.contains(contentType))
            throw new ServiceException("评价图片仅支持 JPG、JPEG、PNG 格式");
        try
        {
            BufferedImage image = decodeVerifiedImage(file, extension);
            ByteArrayOutputStream encoded = new ByteArrayOutputStream();
            if (!ImageIO.write(image, "png", encoded))
                throw new ServiceException("评价图片重新编码失败");

            Path directory = ownedDirectory(memberId);
            Files.createDirectories(directory);
            String randomName = UUID.randomUUID().toString().replace("-", "") + ".png";
            Files.write(directory.resolve(randomName), encoded.toByteArray(), StandardOpenOption.CREATE_NEW);
            return Constants.RESOURCE_PREFIX + "/mall/review/" + memberId + "/" + randomName;
        }
        catch (Exception exception)
        {
            if (exception instanceof ServiceException serviceException) throw serviceException;
            throw new ServiceException("评价图片上传失败，请稍后重试");
        }
    }

    private BufferedImage decodeVerifiedImage(MultipartFile file, String declaredExtension) throws IOException
    {
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(file.getBytes())))
        {
            if (input == null) throw new ServiceException("评价文件不是有效图片");
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) throw new ServiceException("评价文件不是有效图片");
            ImageReader reader = readers.next();
            try
            {
                reader.setInput(input, true, true);
                String detectedFormat = normalizeFormat(reader.getFormatName());
                String extensionFormat = normalizeFormat(declaredExtension);
                String contentTypeFormat = "image/png".equalsIgnoreCase(file.getContentType()) ? "png" : "jpeg";
                if (!detectedFormat.equals(extensionFormat) || !detectedFormat.equals(contentTypeFormat))
                    throw new ServiceException("评价图片格式与声明不一致");
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_DIMENSION || height > MAX_DIMENSION
                        || (long) width * height > MAX_PIXELS)
                    throw new ServiceException("评价图片尺寸不符合要求");
                BufferedImage image = reader.read(0);
                if (image == null) throw new ServiceException("评价文件不是有效图片");
                return image;
            }
            finally
            {
                reader.dispose();
            }
        }
    }

    private String normalizeFormat(String value)
    {
        String format = value == null ? "" : value.toLowerCase(Locale.ROOT);
        return "jpg".equals(format) ? "jpeg" : format;
    }

    private Path ownedDirectory(Long memberId)
    {
        Path profileRoot = Path.of(RuoYiConfig.getProfile()).toAbsolutePath().normalize();
        Path directory = profileRoot.resolve("mall").resolve("review")
                .resolve(String.valueOf(memberId)).normalize();
        if (!directory.startsWith(profileRoot))
            throw new ServiceException("评价图片存储路径无效");
        return directory;
    }

    private String extension(String filename)
    {
        int index = filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
