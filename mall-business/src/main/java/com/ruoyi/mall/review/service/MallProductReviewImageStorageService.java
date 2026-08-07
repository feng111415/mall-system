package com.ruoyi.mall.review.service;

import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.ruoyi.common.utils.file.MimeTypeUtils;

/** 评价图片使用若依资源目录，返回可被现有资源映射读取的 URL。 */
@Service
public class MallProductReviewImageStorageService
{
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024L;
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
            String baseDir = RuoYiConfig.getProfile() + "/mall/review/" + memberId;
            return FileUploadUtils.upload(baseDir, file, MimeTypeUtils.IMAGE_EXTENSION, true);
        }
        catch (Exception exception)
        {
            if (exception instanceof ServiceException serviceException) throw serviceException;
            throw new ServiceException("评价图片上传失败，请稍后重试");
        }
    }

    private String extension(String filename)
    {
        int index = filename.lastIndexOf('.');
        return index < 0 ? "" : filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }
}
