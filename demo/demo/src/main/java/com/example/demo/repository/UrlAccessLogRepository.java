package com.example.demo.repository;

import com.example.demo.entity.UrlAccessLog;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * URL訪問日誌資料庫存取介面
 * 提供URL訪問日誌實體的CRUD操作
 */
public interface UrlAccessLogRepository extends JpaRepository<UrlAccessLog, Long> {
}