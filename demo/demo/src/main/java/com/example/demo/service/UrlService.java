package com.example.demo.service;


import com.example.demo.entity.Url;
import com.example.demo.repository.UrlRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UrlService {

    @Autowired
    private UrlRepository urlRepository;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final String CHAR_POOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SHORT_CODE_LENGTH = 6;

    /**
     * 生成隨機短代碼
     * 使用大小寫字母和數字組成的字符池，生成指定長度的隨機字符串作為短鏈接代碼
     * 
     * @return 生成的6位隨機短代碼
     */
    private String generateShortCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < SHORT_CODE_LENGTH; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    /**
     * 創建短鏈接
     * 為原始URL生成唯一的短代碼，並保存到資料庫中
     * 
     * @param originalUrl 原始URL地址
     * @param expireAt 過期時間，可為null表示永不過期
     * @return 創建的Url實體對象
     */
    public Url createShortUrl(String originalUrl, LocalDateTime expireAt) {
        String shortCode;
        do {
            shortCode = generateShortCode();
        } while (urlRepository.findByShortCode(shortCode).isPresent());

        Url url = new Url(originalUrl, shortCode, expireAt);
        return urlRepository.save(url);
    }

    /**
     * 根據短代碼獲取原始URL
     * 從資料庫中查找對應的短代碼，檢查是否過期，並增加點擊次數
     * 
     * @param shortCode 短鏈接代碼
     * @return 包含原始URL的Optional對象，如果未找到或已過期則為空
     */
    public Optional<Url> getOriginalUrl(String shortCode) {
        return urlRepository.findByShortCode(shortCode)
                .filter(url -> url.getExpireAt() == null || url.getExpireAt().isAfter(LocalDateTime.now()))
                .map(url -> {
                    url.incrementClickCount();
                    return urlRepository.save(url);
                });
    }
    /**
     * 從快取中獲取原始URL
     * 優先從Redis快取中查找URL，如果快取中沒有則從資料庫查詢並存入快取
     * 支援Redis不可用時的降級處理，直接查詢資料庫
     * 
     * @param shortCode 短鏈接代碼
     * @return 原始URL地址，如果未找到則返回null
     */
    public String getOriginalUrlFromCache(String shortCode) {
        String redisKey = "shorturl:" + shortCode;
        String originalUrl = null;
        try {
            originalUrl = (String) redisTemplate.opsForValue().get(redisKey);
        } catch (Exception ignored) {
            // Redis 不可用時，直接走資料庫
        }

        if (originalUrl == null) {
            Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode)
                    .filter(url -> url.getExpireAt() == null || url.getExpireAt().isAfter(LocalDateTime.now()));
            if (urlOpt.isPresent()) {
                originalUrl = urlOpt.get().getOriginalUrl();
                long ttl = urlOpt.get().getExpireAt() != null ?
                        java.time.Duration.between(LocalDateTime.now(), urlOpt.get().getExpireAt()).getSeconds()
                        : 3600;
                try {
                    redisTemplate.opsForValue().set(redisKey, originalUrl, ttl, java.util.concurrent.TimeUnit.SECONDS);
                } catch (Exception ignored) {
                    // Redis 不可用就跳過快取
                }
            }
        }
        return originalUrl;
    }
    /**
     * 增加點擊次數並記錄訪問日誌
     * 在Redis中增加該短鏈接的點擊計數，並通過RabbitMQ發送訪問日誌訊息
     * 用於統計分析和異步處理訪問記錄
     * 
     * @param shortCode 短鏈接代碼
     */
    public void incrementClick(String shortCode) {
        String redisKey = "click:" + shortCode;
        try {
            redisTemplate.opsForValue().increment(redisKey, 1);
        } catch (Exception ignored) {
            // Redis 不可用時忽略
        }

        // 發送訪問日誌到 RabbitMQ
        try {
            java.util.Map<String, Object> log = java.util.Map.of(
                    "shortCode", shortCode,
                    "accessTime", java.time.LocalDateTime.now()
            );
            rabbitTemplate.convertAndSend("access_log_exchange", "access.log", log);
        } catch (Exception ignored) {
        }
    }


}
