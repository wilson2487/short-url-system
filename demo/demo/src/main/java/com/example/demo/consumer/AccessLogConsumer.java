package com.example.demo.consumer;

import com.example.demo.entity.UrlAccessLog;
import com.example.demo.entity.UrlNotification;
import com.example.demo.repository.UrlAccessLogRepository;
import com.example.demo.repository.UrlNotificationRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class AccessLogConsumer {

    @Autowired
    private UrlAccessLogRepository logRepository;
    @Autowired
    private UrlNotificationRepository notificationRepository;

    private static final Logger log = LoggerFactory.getLogger(AccessLogConsumer.class);

    /**
     * 接收訪問日誌消息
     * 從RabbitMQ隊列中接收訪問日誌消息，並保存到資料庫
     * 同時創建相應的通知記錄
     * 
     * @param message 包含短代碼和訪問時間的消息
     */
    @RabbitListener(queues = "access_log_queue")
    public void receive(Map<String, Object> message) {
        try {
            String shortCode = (String) message.get("shortCode");
            LocalDateTime accessTime = coerceToLocalDateTime(message.get("accessTime"));

            UrlAccessLog entity = new UrlAccessLog();
            entity.setShortCode(shortCode);
            entity.setAccessTime(accessTime != null ? accessTime : LocalDateTime.now());
            logRepository.save(entity);

            UrlNotification notification = new UrlNotification();
            notification.setShortCode(shortCode);
            notification.setType("VISIT");
            notification.setMessage("短網址被訪問");
            notificationRepository.save(notification);
        } catch (Exception e) {
            // 不丟出例外，避免 unacked/requeue 造成積壓
            log.warn("[AccessLogConsumer] failed to process message: {}", e.getMessage());
        }
    }

    /**
     * 將對象轉換為LocalDateTime
     * 支援多種時間格式的解析，包括OffsetDateTime、ZonedDateTime、LocalDateTime和ISO字符串
     * 
     * @param raw 原始時間對象
     * @return LocalDateTime 轉換後的時間對象，轉換失敗時返回null
     */
    private LocalDateTime coerceToLocalDateTime(Object raw) {
        if (raw == null) return null;
        if (raw instanceof LocalDateTime t) return t;
        if (raw instanceof String s) {
            String v = s.trim();
            try { return java.time.OffsetDateTime.parse(v).toLocalDateTime(); } catch (Exception ignored) {}
            try { return java.time.ZonedDateTime.parse(v).toLocalDateTime(); } catch (Exception ignored) {}
            try { return java.time.LocalDateTime.parse(v); } catch (Exception ignored) {}
        }
        return null;
    }
}

