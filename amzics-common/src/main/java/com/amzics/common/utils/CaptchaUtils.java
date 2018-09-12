package com.amzics.common.utils;

import com.octo.captcha.Captcha;
import com.octo.captcha.CaptchaFactory;
import com.octo.captcha.component.image.backgroundgenerator.UniColorBackgroundGenerator;
import com.octo.captcha.component.image.color.SingleColorGenerator;
import com.octo.captcha.component.image.fontgenerator.RandomFontGenerator;
import com.octo.captcha.component.image.textpaster.DecoratedRandomTextPaster;
import com.octo.captcha.component.image.textpaster.textdecorator.TextDecorator;
import com.octo.captcha.component.image.wordtoimage.ComposedWordToImage;
import com.octo.captcha.component.word.wordgenerator.RandomWordGenerator;
import com.octo.captcha.engine.GenericCaptchaEngine;
import com.octo.captcha.engine.image.ListImageCaptchaEngine;
import com.octo.captcha.image.gimpy.GimpyFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CaptchaUtils {
    private static Map<String, GenericCaptchaEngine> engineMap = new HashMap<>();


    /**
     * 生成验证码
     *
     * @param length
     * @return
     */
    public static String generateCode(int length) {
        RandomWordGenerator wordGenerator = new RandomWordGenerator("qwertyupasdfghjkzxcvbnmQWERTYUPASDFGHJKZXCVBNM23456789");
        return wordGenerator.getWord(length);
    }

    /**
     * 生成验证码图片
     *
     * @param code   验证码
     * @param width  宽度
     * @param height 高度
     * @return
     */
    public static byte[] generateImage(String code, int width, int height) {
        //字体样式
        RandomFontGenerator fontGenerator = new RandomFontGenerator(26, 34, new Font[]{new Font("Arial", 0, 32)});
        //宽和高
        UniColorBackgroundGenerator backgroundGenerator = new UniColorBackgroundGenerator(width, height);
        //字符长度
        DecoratedRandomTextPaster textPaster = new DecoratedRandomTextPaster(code.length(), code.length(), new SingleColorGenerator(new Color(50, 50, 50)), new TextDecorator[0]);
        //ComposedWordToImage
        ComposedWordToImage word2Image = new ComposedWordToImage(fontGenerator, backgroundGenerator, textPaster);
        BufferedImage image = word2Image.getImage(code);
        // 输出为 byte[]
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 生成验证码图片
     * @param code 验证码
     * @return
     */
    public static byte[] generateImage(String code) {
        return generateImage(code,110,50);
    }
}
