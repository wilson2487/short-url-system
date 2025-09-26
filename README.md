\# 短網址系統 (Short URL System)



一個基於 \*\*Spring Boot\*\* 的簡單短網址系統，可以生成短網址、設置過期時間，並統計點擊次數。



---



\## 功能



\- 輸入原始網址生成短網址

\- 可選擇設定過期時間

\- 點擊短網址自動跳轉到原始網址

\- 過期短網址無法使用（返回 404）

\- 記錄每個短網址的點擊次數

\- 簡單 HTML 前端頁面操作



---



\## 技術棧



\- Java 17+

\- Spring Boot 3

\- Spring Data JPA (資料庫操作)

\- H2 / MySQL（可選）

\- Thymeleaf / 靜態 HTML + JavaScript



---



\## 專案結構



src/

├─ main/

│ ├─ java/com/example/demo/

│ │ ├─ entity/ # Url 實體

│ │ ├─ repository/ # UrlRepository

│ │ ├─ service/ # UrlService

│ │ ├─ controller/ # UrlController, HomeController

│ ├─ resources/

│ ├─ static/ # demo1.html 等前端頁面

│ ├─ application.properties



---



\## 運作流程



1\. 使用者在前端輸入原始網址（可選過期時間）

2\. 前端呼叫 API：`POST /api/url/shorten`

3\. 後端生成短碼、存入資料庫

4\. 返回短網址，例如：

http://localhost:8080/api/url/Ab12Cd

5\. 訪問短網址：

&nbsp;  - 若有效 → 302 重定向到原始網址

&nbsp;  - 若過期或不存在 → 返回 404

6\. 點擊次數自動增加



---



\## 使用方法



1\. 克隆專案：

```bash

git clone https://github.com/你的帳號/short-url-system.git

cd short-url-system

2\.  編譯並啟動：

./mvnw spring-boot:run   # 或使用 IDE 執行主程式

3\. 打開瀏覽器訪問：

http://localhost:8080/  # 前端頁面

4\. 輸入原始網址 → 生成短網址 → 點擊跳轉





API 範例

生成短網址



POST /api/url/shorten

參數：

\- originalUrl (必填)

\- expireAt (可選, ISO-8601 格式)



回傳：

{

&nbsp; "id": 1,

&nbsp; "originalUrl": "https://example.com",

&nbsp; "shortCode": "Ab12Cd",

&nbsp; "expireAt": "2025-09-27T12:00:00",

&nbsp; "clickCount": 0

}

短網址跳轉



GET /api/url/{shortCode}

\- 成功：HTTP 302 重定向到原始網址

\- 過期或不存在：HTTP 404





注意事項



靜態 HTML 頁面放在 src/main/resources/static/



預設短碼長度：6 位字母+數字



點擊次數與過期檢查在後端 Service 層完成



\## 授權

MIT License

