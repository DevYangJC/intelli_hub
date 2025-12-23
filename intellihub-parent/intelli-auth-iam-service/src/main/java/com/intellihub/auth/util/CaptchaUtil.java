package com.intellihub.auth.util;

import com.intellihub.auth.config.AuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Random;

/**
 * 验证码工具类
 *
 * @author intellihub
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class CaptchaUtil {

    private final AuthProperties authProperties;
    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
    private static final Random RANDOM = new Random();

    /**
     * 生成验证码文本
     */
    public String generateCode() {
        int length = authProperties.getCaptcha().getImage().getLength();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * 生成验证码图片（Base64）
     */
    public String generateImage(String code) {
        AuthProperties.CaptchaProperties.ImageCaptcha config = authProperties.getCaptcha().getImage();
        int width = config.getWidth();
        int height = config.getHeight();

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 设置背景色
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, width, height);

        // 绘制干扰线
        g.setColor(new Color(200, 200, 200));
        for (int i = 0; i < 5; i++) {
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }

        // 绘制验证码
        g.setFont(new Font("Arial", Font.BOLD, 24));
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(RANDOM.nextInt(100), RANDOM.nextInt(100), RANDOM.nextInt(100)));
            int x = 15 + i * 25;
            int y = 25 + RANDOM.nextInt(10);
            g.drawString(String.valueOf(code.charAt(i)), x, y);
        }

        // 绘制噪点
        for (int i = 0; i < 50; i++) {
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            g.setColor(new Color(RANDOM.nextInt(255), RANDOM.nextInt(255), RANDOM.nextInt(255)));
            g.drawOval(x, y, 1, 1);
        }

        g.dispose();

        // 转换为Base64
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("生成验证码图片失败", e);
        }
    }

    /**
     * 获取验证码过期时间（秒）
     */
    public Integer getExpiration() {
        return authProperties.getCaptcha().getImage().getExpiration();
    }

    /**
     * 验证码是否启用
     */
    public boolean isEnabled() {
        return authProperties.getCaptcha().getEnabled();
    }
}
