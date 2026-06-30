package com.parttime.common.util;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

/**
 * AES-256加密工具类
 * <p>
 * 使用AES/CBC/PKCS5Padding模式进行加密，密钥从环境变量AES_SECRET_KEY读取（Base64编码的32字节密钥）。
 * 每次加密使用随机IV，IV拼接在密文前16字节，解密时自动提取。
 * </p>
 * <p>
 * 适用场景：敏感字段（身份证、手机号、银行卡）的加密存储。
 * </p>
 */
public class AesUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private static volatile SecretKeySpec secretKeySpec;

    /**
     * 获取AES密钥规格
     * <p>
     * 使用双重检查锁确保线程安全，密钥从环境变量AES_SECRET_KEY读取，
     * 如果环境变量未设置，使用默认密钥（仅用于开发环境）。
     * </p>
     *
     * @return SecretKeySpec AES密钥规格
     * @throws IllegalArgumentException 密钥长度不为32字节时抛出
     */
    private static SecretKeySpec getSecretKeySpec() {
        if (secretKeySpec == null) {
            synchronized (AesUtil.class) {
                if (secretKeySpec == null) {
                    String key = System.getenv("AES_SECRET_KEY");
                    byte[] keyBytes;
                    if (key == null || key.isEmpty()) {
                        keyBytes = new byte[32];
                        for (int i = 0; i < 32; i++) {
                            keyBytes[i] = (byte) (i + 1);
                        }
                    } else {
                        keyBytes = Base64.decodeBase64(key);
                        if (keyBytes.length != 32) {
                            throw new IllegalArgumentException("AES密钥必须是32字节(Base64编码)");
                        }
                    }
                    secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
                }
            }
        }
        return secretKeySpec;
    }

    /**
     * 加密明文
     * <p>
     * 使用AES/CBC/PKCS5Padding模式加密，生成随机IV并拼接在密文前16字节，
     * 最终返回Base64编码的密文。
     * </p>
     *
     * @param plainText 明文
     * @return Base64编码的密文，若输入为空则返回原值
     * @throws RuntimeException 加密失败时抛出
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKeySpec(), new IvParameterSpec(iv));

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[IV_LENGTH + encryptedBytes.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(encryptedBytes, 0, result, IV_LENGTH, encryptedBytes.length);

            return Base64.encodeBase64String(result);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * 解密密文
     * <p>
     * 从Base64编码的密文中提取前16字节作为IV，剩余部分作为加密数据进行解密，
     * 返回UTF-8编码的明文。
     * </p>
     *
     * @param cipherText Base64编码的密文
     * @return UTF-8编码的明文，若输入为空则返回原值
     * @throws RuntimeException 解密失败时抛出（密钥错误、密文损坏等）
     */
    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            byte[] decoded = Base64.decodeBase64(cipherText);

            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, IV_LENGTH);

            byte[] encryptedBytes = new byte[decoded.length - IV_LENGTH];
            System.arraycopy(decoded, IV_LENGTH, encryptedBytes, 0, encryptedBytes.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKeySpec(), new IvParameterSpec(iv));

            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }
}