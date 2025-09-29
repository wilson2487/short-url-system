package com.example.demo.scheduler;


import com.example.demo.repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ClickCountSyncScheduler {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UrlRepository urlRepository;

    private static final Logger log = LoggerFactory.getLogger(ClickCountSyncScheduler.class);

    /**
     * 同步點擊次數
     * 定期將Redis中的點擊計數同步到資料庫
     * 每5秒執行一次，確保點擊數據的一致性
     */
    @Scheduled(fixedRate = 5000) // 每 5 秒執行一次，方便驗證
    public void syncClickCount() {
        Set<String> keys;
        try {
            keys = redisTemplate.keys("click:*");
        } catch (Exception e) {
            log.warn("[syncClickCount] fetch keys failed: {}", e.getMessage());
            return;
        }
        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                String[] parts = key.split(":", 2);
                if (parts.length < 2) continue;
                String shortCode = parts[1];

                Object raw = redisTemplate.opsForValue().get(key);
                Long count = coerceToLong(raw);
                if (count == null || count <= 0) continue;

                urlRepository.findByShortCode(shortCode).ifPresent(url -> {
                    long newCount = (url.getClickCount() != null ? url.getClickCount() : 0L) + count;
                    url.setClickCount(newCount);
                    urlRepository.save(url);
                    try { redisTemplate.delete(key); } catch (Exception ignored) {}
                    log.info("[syncClickCount] synced {} -> +{} (total={})", shortCode, count, newCount);
                });
            } catch (Exception e) {
                log.warn("[syncClickCount] failed for key {}: {}", key, e.getMessage());
            }
        }
    }

    /**
     * 將對象轉換為Long類型
     * 支援多種數據類型的轉換，包括Long、Integer、String和byte數組
     * 
     * @param raw 原始對象
     * @return Long 轉換後的長整型值，轉換失敗時返回null
     */
    private Long coerceToLong(Object raw) {
        if (raw == null) return null;
        if (raw instanceof Long l) return l;
        if (raw instanceof Integer i) return i.longValue();
        if (raw instanceof String s) {
            try { return Long.parseLong(s); } catch (NumberFormatException ignore) {}
        }
        if (raw instanceof byte[] b) {
            try { return Long.parseLong(new String(b)); } catch (NumberFormatException ignore) {}
        }
        return null;
    }
}
