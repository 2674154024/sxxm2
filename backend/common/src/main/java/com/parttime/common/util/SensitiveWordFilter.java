package com.parttime.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class SensitiveWordFilter {

    private static final String SENSITIVE_WORDS_FILE = "sensitive-words.txt";
    private static final long REFRESH_INTERVAL_MINUTES = 60;

    private static SensitiveWordFilter instance;

    private final Set<String> sensitiveWords = new HashSet<>();
    private volatile Map<String, Object> sensitiveWordTree;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    @PostConstruct
    public void init() {
        instance = this;
        loadSensitiveWords();
        scheduler.scheduleAtFixedRate(this::loadSensitiveWords, REFRESH_INTERVAL_MINUTES, REFRESH_INTERVAL_MINUTES, TimeUnit.MINUTES);
    }

    private void loadSensitiveWords() {
        Set<String> newWords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource(SENSITIVE_WORDS_FILE).getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    newWords.add(word);
                }
            }
            this.sensitiveWords.clear();
            this.sensitiveWords.addAll(newWords);
            this.sensitiveWordTree = buildSensitiveWordTree();
            log.info("敏感词库加载完成，共{}个敏感词", newWords.size());
        } catch (Exception e) {
            log.error("加载敏感词库失败，使用默认词库", e);
            loadDefaultWords();
        }
    }

    private void loadDefaultWords() {
        String[] defaultWords = {
                "押金", "培训费", "中介费", "高薪零门槛", "垫资", "刷单", "先交钱",
                "充值", "缴费", "保证金", "押金返还", "退费", "贷款", "征信"
        };
        this.sensitiveWords.clear();
        this.sensitiveWords.addAll(Arrays.asList(defaultWords));
        this.sensitiveWordTree = buildSensitiveWordTree();
        log.info("使用默认敏感词库，共{}个敏感词", defaultWords.length);
    }

    private Map<String, Object> buildSensitiveWordTree() {
        Map<String, Object> tree = new HashMap<>();
        for (String word : sensitiveWords) {
            Map<String, Object> current = tree;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                String key = String.valueOf(c);
                if (!current.containsKey(key)) {
                    current.put(key, new HashMap<String, Object>());
                }
                current = (Map<String, Object>) current.get(key);
                if (i == word.length() - 1) {
                    current.put("isEnd", true);
                }
            }
        }
        return tree;
    }

    private Map<String, Object> getSensitiveWordTree() {
        if (sensitiveWordTree == null) {
            synchronized (SensitiveWordFilter.class) {
                if (sensitiveWordTree == null) {
                    loadSensitiveWords();
                }
            }
        }
        return sensitiveWordTree;
    }

    public static boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return !getSensitiveWords(text).isEmpty();
    }

    public static List<String> getSensitiveWords(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> result = new ArrayList<>();
        Map<String, Object> tree = instance != null ? instance.getSensitiveWordTree() : buildDefaultTree();

        for (int i = 0; i < text.length(); i++) {
            Map<String, Object> current = tree;
            StringBuilder matchedWord = new StringBuilder();

            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                String key = String.valueOf(c);

                if (!current.containsKey(key)) {
                    break;
                }

                matchedWord.append(c);
                current = (Map<String, Object>) current.get(key);

                if (Boolean.TRUE.equals(current.get("isEnd"))) {
                    String word = matchedWord.toString();
                    if (!result.contains(word)) {
                        result.add(word);
                    }
                }
            }
        }

        return result;
    }

    private static Map<String, Object> buildDefaultTree() {
        String[] defaultWords = {
                "押金", "培训费", "中介费", "高薪零门槛", "垫资", "刷单", "先交钱",
                "充值", "缴费", "保证金", "押金返还", "退费", "贷款", "征信"
        };
        Map<String, Object> tree = new HashMap<>();
        for (String word : defaultWords) {
            Map<String, Object> current = tree;
            for (int i = 0; i < word.length(); i++) {
                char c = word.charAt(i);
                String key = String.valueOf(c);
                if (!current.containsKey(key)) {
                    current.put(key, new HashMap<String, Object>());
                }
                current = (Map<String, Object>) current.get(key);
                if (i == word.length() - 1) {
                    current.put("isEnd", true);
                }
            }
        }
        return tree;
    }
}