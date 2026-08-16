# News Feed 讀書會 Demo

以「設計新聞提要系統」為主題的第一階段讀書會實作。使用者可註冊、登入、發送純文字貼文，並在登入後查看最新動態牆。

## 技術組成

- 後端：Java 26、Spring Boot、Spring Security、JWT
- 前端：React、TypeScript、Vite
- 資料庫：PostgreSQL
- 快取：Redis
- 訊息佇列：RabbitMQ
- 容器化：Docker Compose

## 第一階段資料流

```text
登入 → HttpOnly auth_token Cookie（JWT）
發文 → PostgreSQL
     → RabbitMQ post.created 事件
     → Fanout worker
     → Redis feed:global
讀取 Feed → Redis 取得貼文排序 → PostgreSQL 取得完整貼文與作者
```

## 環境

專案提供兩套可同時運行、資料彼此隔離的環境：

- `local`：IntelliJ + Vite，用於本機開發與中斷點除錯。

## 注意事項

`.env` 保存此電腦實際使用的資料庫密碼、RabbitMQ 密碼與 JWT secret，已被 Git 忽略，絕對不可提交。公開 repository 只保留沒有可用 secret 的 `.env.example`。

本專案僅供讀書會 Demo。正式公開服務仍需啟用 secure Cookie、CSRF 防護、限流與更完整的權限設計。
