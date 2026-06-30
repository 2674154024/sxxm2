package com.parttime.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AesUtilTest {

    @Test
    public void testEncryptDecrypt() {
        String plainText = "test1234567890";
        String encrypted = AesUtil.encrypt(plainText);
        assertNotNull(encrypted);
        assertFalse(encrypted.isEmpty());
        String decrypted = AesUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }

    @Test
    public void testEmptyString() {
        assertEquals("", AesUtil.encrypt(""));
        assertEquals("", AesUtil.decrypt(""));
    }

    @Test
    public void testNullString() {
        assertNull(AesUtil.encrypt(null));
        assertNull(AesUtil.decrypt(null));
    }

    @Test
    public void testChinese() {
        String plainText = "长沙大学生兼职平台";
        String encrypted = AesUtil.encrypt(plainText);
        assertNotNull(encrypted);
        String decrypted = AesUtil.decrypt(encrypted);
        assertEquals(plainText, decrypted);
    }
}