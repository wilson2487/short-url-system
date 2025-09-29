package com.example.demo.controller;

import com.example.demo.entity.Url;
import com.example.demo.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.Instant;
import java.time.format.DateTimeParseException;
 

@RestController
@RequestMapping("/api/url")
public class UrlController {

    @Autowired
    private UrlService urlService;

    /**
     * 生成短網址
     * 接收原始URL並生成對應的短鏈接，支援設定過期時間
     * 只接受JSON格式的請求
     * 
     * @param request 包含原始URL和過期時間的請求對象
     * @return ResponseEntity<Url> 生成的短鏈接實體，或錯誤響應
     */
    @PostMapping(value = "/shorten", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Url> shortenUrl(@RequestBody ShortenRequest request) {
        if (request == null || request.getOriginalUrl() == null || request.getOriginalUrl().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        LocalDateTime expireAt = null;
        String expireAtRaw = request.getExpireAt();
        if (expireAtRaw != null && !expireAtRaw.isBlank()) {
            String raw = expireAtRaw.trim();
            try {
                expireAt = OffsetDateTime.parse(raw).toLocalDateTime();
            } catch (DateTimeParseException e1) {
                try {
                    expireAt = LocalDateTime.parse(raw);
                } catch (DateTimeParseException e2) {
                    try {
                        expireAt = ZonedDateTime.parse(raw).toLocalDateTime();
                    } catch (DateTimeParseException e3) {
                        try {
                            expireAt = LocalDateTime.ofInstant(Instant.parse(raw), java.time.ZoneOffset.UTC);
                        } catch (DateTimeParseException e4) {
                            expireAt = null;
                        }
                    }
                }
            }
        }

        Url url = urlService.createShortUrl(request.getOriginalUrl(), expireAt);
        return ResponseEntity.ok(url);
    }

    /**
     * 透過短碼重定向到原始URL
     * 根據短代碼查找原始URL並進行重定向，同時記錄訪問日誌
     * 
     * @param shortCode 短鏈接代碼
     * @param ip 用戶IP地址（從X-Forwarded-For頭獲取）
     * @param ua 用戶代理字符串
     * @param referer 來源頁面URL
     * @return ResponseEntity 重定向響應或錯誤信息
     */
    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(
            @PathVariable String shortCode,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip,
            @RequestHeader(value = "User-Agent") String ua,
            @RequestHeader(value = "Referer", required = false) String referer
    ) {
        String originalUrl = urlService.getOriginalUrlFromCache(shortCode);
        if (originalUrl == null) {
            return ResponseEntity.status(404).body("短網址不存在或已過期");
        }

        // 記錄點擊
        urlService.incrementClick(shortCode);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(originalUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}

class ShortenRequest {
    private String originalUrl;
    private String expireAt; // ISO-8601，例如 2025-09-27T12:00:00Z

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }
    public String getExpireAt() { return expireAt; }
    public void setExpireAt(String expireAt) { this.expireAt = expireAt; }
}

