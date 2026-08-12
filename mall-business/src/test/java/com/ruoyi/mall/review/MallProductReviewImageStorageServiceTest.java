package com.ruoyi.mall.review;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.mall.review.service.MallProductReviewImageStorageService;

class MallProductReviewImageStorageServiceTest
{
    @TempDir Path profileRoot;
    private MallProductReviewImageStorageService service;

    @BeforeEach
    void setUp()
    {
        new RuoYiConfig().setProfile(profileRoot.toString());
        service = new MallProductReviewImageStorageService();
    }

    @Test
    void rejectsNonImageContentWithImageDeclaration()
    {
        MockMultipartFile forged = new MockMultipartFile(
                "file", "forged.png", "image/png", "not-an-image".getBytes(StandardCharsets.UTF_8));

        assertThrows(ServiceException.class, () -> service.store(7L, forged));
    }

    @Test
    void reencodesImageWithoutTrailingPayloadOrOriginalName() throws Exception
    {
        String marker = "PRIVATE_TRAILING_PAYLOAD";
        MockMultipartFile file = new MockMultipartFile(
                "file", "customer-real-name.png", "image/png", pngWithTrailingMarker(marker));

        String imageUrl = service.store(7L, file);

        assertTrue(imageUrl.matches("^/profile/mall/review/7/[0-9a-f]{32}\\.png$"));
        assertFalse(imageUrl.contains("customer-real-name"));
        Path stored = profileRoot.resolve(imageUrl.substring("/profile/".length())
                .replace('/', java.io.File.separatorChar));
        assertTrue(Files.exists(stored));
        assertFalse(new String(Files.readAllBytes(stored), StandardCharsets.ISO_8859_1).contains(marker));
    }

    private byte[] pngWithTrailingMarker(String marker) throws Exception
    {
        BufferedImage image = new BufferedImage(120, 80, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(new Color(31, 111, 95));
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
        graphics.dispose();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, "png", output);
        output.write(marker.getBytes(StandardCharsets.ISO_8859_1));
        return output.toByteArray();
    }
}
