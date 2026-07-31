package com.ruoyi.mall.member;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.mall.member.adapter.LocalMallAvatarStorageAdapter;
import com.ruoyi.common.exception.ServiceException;

class LocalMallAvatarStorageAdapterTest
{
    @TempDir Path profileRoot;

    @Test
    void storesCroppedReencodedPngWithoutInputMetadataOrOriginalName() throws Exception
    {
        byte[] source = pngWithTrailingMarker(800, 400, "PRIVATE_METADATA");
        MockMultipartFile file = new MockMultipartFile(
                "file", "my-real-name.png", "image/png", source);
        LocalMallAvatarStorageAdapter adapter = new LocalMallAvatarStorageAdapter(profileRoot.toString());

        String avatarUrl = adapter.store(7L, file);

        assertTrue(avatarUrl.matches("^/profile/mall/avatar/7/[0-9a-f]{32}\\.png$"));
        assertFalse(avatarUrl.contains("my-real-name"));
        Path storedFile = profileRoot.resolve(avatarUrl.substring("/profile/".length()).replace('/', java.io.File.separatorChar));
        BufferedImage storedImage = ImageIO.read(storedFile.toFile());
        assertEquals(512, storedImage.getWidth());
        assertEquals(512, storedImage.getHeight());
        assertFalse(new String(Files.readAllBytes(storedFile), StandardCharsets.ISO_8859_1)
                .contains("PRIVATE_METADATA"));
    }

    @Test
    void rejectsImageWhoseContentDoesNotMatchDeclaredFormat() throws Exception
    {
        MockMultipartFile forgedFile = new MockMultipartFile(
                "file", "forged.png", "image/png", imageBytes(300, 300, "jpg"));
        LocalMallAvatarStorageAdapter adapter = new LocalMallAvatarStorageAdapter(profileRoot.toString());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> adapter.store(7L, forgedFile));

        assertEquals("头像文件格式与声明不一致", exception.getMessage());
    }

    @Test
    void rejectsNonImageContent()
    {
        MockMultipartFile textFile = new MockMultipartFile(
                "file", "fake.png", "image/png", "not-an-image".getBytes(StandardCharsets.UTF_8));
        LocalMallAvatarStorageAdapter adapter = new LocalMallAvatarStorageAdapter(profileRoot.toString());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> adapter.store(7L, textFile));

        assertEquals("头像文件不是有效图片", exception.getMessage());
    }

    @Test
    void rejectsAvatarLargerThanFiveMegabytes()
    {
        MockMultipartFile oversizedFile = new MockMultipartFile(
                "file", "large.png", "image/png", new byte[5 * 1024 * 1024 + 1]);
        LocalMallAvatarStorageAdapter adapter = new LocalMallAvatarStorageAdapter(profileRoot.toString());

        ServiceException exception = assertThrows(ServiceException.class,
                () -> adapter.store(7L, oversizedFile));

        assertEquals("头像图片不能超过 5MB", exception.getMessage());
    }

    private byte[] pngWithTrailingMarker(int width, int height, String marker) throws Exception
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(31, 111, 95));
        graphics.fillRect(0, 0, width, height);
        graphics.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        output.write(marker.getBytes(StandardCharsets.ISO_8859_1));
        return output.toByteArray();
    }

    private byte[] imageBytes(int width, int height, String format) throws Exception
    {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }
}
