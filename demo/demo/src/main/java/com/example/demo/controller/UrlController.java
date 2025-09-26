package com.example.demo.controller;

import com.example.demo.entity.Url;
import com.example.demo.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/url")
public class UrlController {

    @Autowired
    private UrlService urlService;

    // 生成短網址，可選擇過期時間
    @PostMapping("/shorten")
    public ResponseEntity<Url> shortenUrl(
            @RequestParam String originalUrl,
            @RequestParam(required = false) LocalDateTime expireAt) {
        Url url = urlService.createShortUrl(originalUrl, expireAt);
        return ResponseEntity.ok(url);
    }

    // 透過短碼重定向
    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        return urlService.getOriginalUrl(shortCode)
                .map(url -> ResponseEntity.status(302).header("Location", url.getOriginalUrl()).build())
                .orElse(ResponseEntity.notFound().build());
    }
}

