package com.parttime.common.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SensitiveWordFilterTest {

    @Test
    public void testContainsSensitiveWord() {
        assertTrue(SensitiveWordFilter.containsSensitiveWord("本岗位需要缴纳押金"));
        assertTrue(SensitiveWordFilter.containsSensitiveWord("培训费500元"));
        assertTrue(SensitiveWordFilter.containsSensitiveWord("中介费300元"));
        assertTrue(SensitiveWordFilter.containsSensitiveWord("高薪零门槛工作"));
        assertTrue(SensitiveWordFilter.containsSensitiveWord("垫资刷单"));
        assertFalse(SensitiveWordFilter.containsSensitiveWord("正常兼职工作"));
        assertFalse(SensitiveWordFilter.containsSensitiveWord("时薪17元"));
    }

    @Test
    public void testGetSensitiveWords() {
        List<String> words = SensitiveWordFilter.getSensitiveWords("押金和培训费都要交");
        assertTrue(words.contains("押金"));
        assertTrue(words.contains("培训费"));

        words = SensitiveWordFilter.getSensitiveWords("正常工作内容");
        assertTrue(words.isEmpty());
    }

    @Test
    public void testEmptyText() {
        assertFalse(SensitiveWordFilter.containsSensitiveWord(""));
        assertTrue(SensitiveWordFilter.getSensitiveWords("").isEmpty());
    }

    @Test
    public void testNullText() {
        assertFalse(SensitiveWordFilter.containsSensitiveWord(null));
        assertTrue(SensitiveWordFilter.getSensitiveWords(null).isEmpty());
    }
}