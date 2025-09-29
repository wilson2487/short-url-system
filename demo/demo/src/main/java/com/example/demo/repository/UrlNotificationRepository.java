package com.example.demo.repository;

import com.example.demo.entity.UrlNotification;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * URL通知資料庫存取介面
 * 提供URL通知實體的CRUD操作
 */
public interface UrlNotificationRepository extends JpaRepository<UrlNotification, Long> {
}