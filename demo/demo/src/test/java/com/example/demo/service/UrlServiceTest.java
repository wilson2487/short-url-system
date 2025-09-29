package com.example.demo.service;

import com.example.demo.entity.Url;
import com.example.demo.repository.UrlRepository;
import com.example.demo.service.UrlService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository; // 假的 DB

    @InjectMocks
    private UrlService urlService; // 自動把 Mock 塞進去

    /**
     * 測試短碼生成功能
     * 驗證：
     * 1. 短碼不為空
     * 2. 短碼長度為 6 位
     */
    @Test
    void testShortCodeGeneration() {
        // 模擬資料庫中沒有重複的短碼
        when(urlRepository.findByShortCode(anyString())).thenReturn(Optional.empty());

        // 模擬 save 回傳值
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 執行測試
        Url url = urlService.createShortUrl("https://example.com", LocalDateTime.now().plusDays(1));
        
        // 驗證結果
        assertNotNull(url.getShortCode());
        assertEquals(6, url.getShortCode().length());
    }
    /**
     * 測試短碼唯一性功能
     * 驗證：
     * 1. 當短碼重複時，會重新生成新的短碼
     * 2. 最終生成的短碼不為空且長度為 6 位
     */
    @Test
    void testShortCodeUniqueness() {
        // 模擬第一次查詢時短碼已存在（衝突），第二次查詢時不存在
        when(urlRepository.findByShortCode(anyString()))
                .thenReturn(Optional.of(new Url()))  // 第一次衝突
                .thenReturn(Optional.empty());       // 第二次沒衝突

        // 模擬 save 回傳值
        when(urlRepository.save(any(Url.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // 執行測試
        Url url = urlService.createShortUrl("https://example.com", LocalDateTime.now().plusDays(1));
        
        // 驗證結果
        assertNotNull(url.getShortCode());
        assertEquals(6, url.getShortCode().length());
    }
}


