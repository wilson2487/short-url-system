package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "short_url", schema = "url_shortener")
public class Url {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 20)
    private String shortCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    /**
     * 預設建構子
     * 用於JPA實體映射
     */
    public Url() {}

    /**
     * 建構子
     * 創建URL實體時初始化基本屬性
     * 
     * @param originalUrl 原始URL地址
     * @param shortCode 短鏈接代碼
     * @param expireAt 過期時間
     */
    public Url(String originalUrl, String shortCode, LocalDateTime expireAt) {
        this.originalUrl = originalUrl;
        this.shortCode = shortCode;
        this.expireAt = expireAt;
    }

    /**
     * Getters & Setters
     * 實體屬性的存取方法
     */
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getShortCode() { return shortCode; }
    public void setShortCode(String shortCode) { this.shortCode = shortCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }

    public Long getClickCount() { return clickCount; }
    public void setClickCount(Long clickCount) { this.clickCount = clickCount; }
    /**
     * 增加點擊次數
     * 將指定數量加到當前點擊次數上
     * 
     * @param count 要增加的量
     */
    public void addClickCount(Long count) {
        this.clickCount += count;
    }

    /**
     * 增加一次點擊
     * 將點擊次數加1
     */
    public void incrementClickCount() {
        this.clickCount++;
    }
}

