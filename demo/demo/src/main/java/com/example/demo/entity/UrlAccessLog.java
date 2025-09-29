package com.example.demo.entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * URL訪問日誌實體
 * 記錄短鏈接的訪問信息，包括用戶IP、User-Agent、來源頁面等
 */
@Entity
@Table(name = "url_access_log")
public class UrlAccessLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code", length = 20)
    private String shortCode;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserIp() {
        return userIp;
    }

    public void setUserIp(String userIp) {
        this.userIp = userIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    public LocalDateTime getAccessTime() {
        return accessTime;
    }

    public void setAccessTime(LocalDateTime accessTime) {
        this.accessTime = accessTime;
    }

    @Column(name = "user_ip", length = 50)
    private String userIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(name = "referer", length = 255)
    private String referer;

    @Column(name = "access_time")
    private LocalDateTime accessTime = LocalDateTime.now();

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }
}
