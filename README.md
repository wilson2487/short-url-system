# 短網址系統 (Short URL System)

一個基於 **Spring Boot** 的高效能短網址系統，支援 Redis 快取、RabbitMQ 消息佇列、訪問日誌記錄等功能。



---



## 功能

- 輸入原始網址生成短網址
- 可選擇設定過期時間
- 點擊短網址自動跳轉到原始網址
- 過期短網址無法使用（返回 404）
- 記錄每個短網址的點擊次數
- **Redis 快取機制**：提升短網址查詢效能
- **RabbitMQ 消息佇列**：異步處理訪問日誌
- **訪問日誌記錄**：記錄用戶IP、User-Agent、來源頁面等詳細信息
- **通知系統**：自動生成訪問通知記錄
- **定時同步**：定期將Redis中的點擊計數同步到資料庫
- 簡單 HTML 前端頁面操作



---



## 技術棧

- Java 17+
- Spring Boot 3
- Spring Data JPA (資料庫操作)
- **Redis** (快取與計數器)
- **RabbitMQ** (消息佇列)
- MySQL (資料庫)
- 靜態 HTML + JavaScript



---



## 專案結構

```
src/
├─ main/
│ ├─ java/com/example/demo/
│ │ ├─ entity/           # Url, UrlAccessLog, UrlNotification 實體
│ │ ├─ repository/       # UrlRepository, UrlAccessLogRepository, UrlNotificationRepository
│ │ ├─ service/          # UrlService
│ │ ├─ controller/       # UrlController, HomeController
│ │ ├─ config/           # RabbitConfig, RedisConfig
│ │ ├─ consumer/         # AccessLogConsumer
│ │ ├─ scheduler/        # ClickCountSyncScheduler
│ │ └─ DemoApplication.java
│ └─ resources/
│   ├─ static/           # demo1.html 等前端頁面
│   └─ application.properties
└─ test/
  └─ java/com/example/demo/
    └─ DemoApplicationTests.java
```



---



## 運作流程

1. 使用者在前端輸入原始網址（可選過期時間）
2. 前端呼叫 API：`POST /api/url/shorten`
3. 後端生成短碼、存入資料庫
4. 返回短網址，例如：`http://localhost:8080/api/url/Ab12Cd`
5. 訪問短網址：
   - 優先從 **Redis 快取** 查詢原始網址
   - 快取未命中時從資料庫查詢並寫入快取
   - 若有效 → 302 重定向到原始網址
   - 若過期或不存在 → 返回 404
6. **異步處理**：
   - 點擊次數在 Redis 中即時增加
   - 發送訪問日誌到 **RabbitMQ 消息佇列**
   - 消費者異步處理，記錄詳細訪問信息
   - 生成訪問通知記錄
7. **定時同步**：每5秒將 Redis 中的點擊計數同步到資料庫



---



## 使用方法

### 前置需求
- Java 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+

### 安裝步驟

1. **克隆專案**：
```bash
git clone https://github.com/your-username/short-url-system.git
cd short-url-system
```

2. **啟動依賴服務**：
```bash
# 啟動 Redis
docker run -d --name redis -p 6379:6379 redis:latest

# 啟動 RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management

# 啟動 MySQL (或使用本地安裝)
docker run -d --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=url_shortener mysql:8.0
```

3. **配置資料庫**：
```sql
CREATE DATABASE url_shortener;
```

4. **配置應用**：
```bash
# 複製配置範例文件
cp application-example.properties application.properties

# 編輯配置文件，修改資料庫、Redis、RabbitMQ 的連接信息
```

5. **編譯並啟動**：
```bash
./mvnw spring-boot:run
```

6. **訪問應用**：
- 前端頁面：http://localhost:8080/
- API 文檔：http://localhost:8080/api/url/shorten





## API 文檔

### 生成短網址

**POST** `/api/url/shorten`

**請求參數**：
```json
{
  "originalUrl": "https://example.com",
  "expireAt": "2025-12-31T23:59:59"  // 可選，ISO-8601 格式
}
```

**回應**：
```json
{
  "id": 1,
  "originalUrl": "https://example.com",
  "shortCode": "Ab12Cd",
  "expireAt": "2025-12-31T23:59:59",
  "clickCount": 0,
  "createdAt": "2024-01-01T12:00:00"
}
```

### 短網址跳轉

**GET** `/api/url/{shortCode}`

**回應**：
- 成功：HTTP 302 重定向到原始網址
- 過期或不存在：HTTP 404





## 系統架構

### 快取策略
- **Redis 快取**：短網址查詢優先從快取獲取，提升響應速度
- **TTL 設置**：快取過期時間與短網址過期時間同步
- **降級處理**：Redis 不可用時自動降級到資料庫查詢

### 消息佇列
- **RabbitMQ**：異步處理訪問日誌，避免阻塞主流程
- **JSON 序列化**：消息使用 JSON 格式傳輸
- **錯誤處理**：消息處理失敗時記錄日誌，避免消息積壓

### 定時任務
- **點擊計數同步**：每5秒將 Redis 計數器同步到資料庫
- **批量處理**：支援多個短網址的計數同步
- **容錯機制**：Redis 或資料庫異常時不影響系統運行

## 注意事項

- 靜態 HTML 頁面放在 `src/main/resources/static/`
- 預設短碼長度：6 位字母+數字
- 點擊次數與過期檢查在後端 Service 層完成
- **Redis 和 RabbitMQ 為可選依賴**：系統會自動降級處理

## 授權

MIT License

