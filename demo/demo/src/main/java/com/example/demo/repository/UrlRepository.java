package com.example.demo.repository;


import com.example.demo.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * URL資料庫存取介面
 * 提供URL實體的CRUD操作和自定義查詢方法
 */
public interface UrlRepository extends JpaRepository<Url, Long> {
    
    /**
     * 根據短代碼查找URL
     * 
     * @param shortCode 短鏈接代碼
     * @return Optional<Url> 包含URL的Optional對象，如果未找到則為空
     */
    Optional<Url> findByShortCode(String shortCode);
}

