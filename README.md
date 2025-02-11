# 物流系統

## 概述
這是一個使用 Spring Boot 構建的物流系統，採用了微服務架構。系統旨在處理 訂單管理、物流處理、配送任務、通知發送 和 用戶管理。
每個模塊通過 RabbitMQ 進行異步通信，結構設計上注重可擴展性和可維護性。

## 技術選型
- **後端技術**：
  - Spring Boot
  - Spring Data JPA
  - H2 Database
  - MySQL
  - Redis
  - Spring AMQP (RabbitMQ)
- **消息佇列**：
  - RabbitMQ
- **認證與授權**：
  - Spring Security (JWT)
- **API 文件**：
  - Swagger/OpenAPI
- **測試**：
  - Spring Test
## 系統架構
- **訂單服務（Order Service）**:
    ```
    處理 訂單創建、查詢、更新 和 取消。
    訂單創建後將消息發送到 RabbitMQ。
    ```
- **物流服務（Logistics Service）**:
    ```
    負責 包裹分揀、路由規劃 和 配送狀態更新。
    從 RabbitMQ 接收訂單消息並進行處理。
    ```
- **配送服務（Delivery Service）**:
    ```
    管理 配送員、配送任務分配 和 實時追蹤。
    在配送狀態更新後將消息發送回訂單服務。
    ```
- **通知服務（Notification Service）**:
    ```
    根據物流狀態更新發送 郵件通知。
    訂閱相關的 RabbitMQ Topic 進行處理。
    ```
- **用戶服務（User Service）**:
    ```
    管理 客戶 和 配送員 帳號資訊。
    擁有 認證 和 授權 功能，使用 JWT 進行身份驗證。
    ```
- **共用模塊（Common Module）**:
    ```
    包含 共享代碼，例如 DTO、服務類、異常處理、工具類和安全配置（JWT）。
    ```
# 目錄結構
```plaintext
├── order-service
│   ├── src/main/java/com/example/orderservice
│   ├── src/main/resources
│   │   └── application.properties
│
├── logistics-service
│   ├── src/main/java/com/example/logisticsservice
│   ├── src/main/resources
│   │   └── application.properties
│
├── delivery-service
│   ├── src/main/java/com/example/deliveryservice
│   ├── src/main/resources
│   │   └── application.properties
│
├── notification-service
│   ├── src/main/java/com/example/notificationservice
│   ├── src/main/resources
│   │   └── application.properties
│
├── user-service
│   ├── src/main/java/com/example/userservice
│   │   ├── controller  (用戶API接口處理)
│   │   ├── service     (用戶邏輯，認證與授權)
│   │   ├── repository  (用戶資料庫交互)
│   │   ├── entity      (用戶相關模型)
│   │   ├── event       (用戶事件模型)
│   │   ├── dto         (用戶相關DTO)
│   │   └── config      (RabbitMQ, JWT配置等)
│   ├── src/main/resources
│   │   └── application.properties
│
├── common
│   ├── src/main/java/com/example/common
│   │   ├── dto         (共享DTO)
│   │   ├── service     (共享服務類)
│   │   ├── exception   (異常處理類)
│   │   ├── util        (工具類)
│   │   └── security    (安全相關類)
│   ├── src/main/resources
│   │   ├── application.properties
│   │   ├── application-dev.properties
│   │   └── application-prod.properties
│
└── pom.xml (父目錄的構建文件)

```
## 服務間通信
- 各個服務通過 RabbitMQ 進行異步通信。每個服務會訂閱特定的隊列，並根據消息進行處理。
- 訂單服務 創建並更新訂單，物流服務 和 配送服務 負責處理訂單。
- 通知服務 根據物流狀態的更新發送通知。
- 用戶服務 管理所有服務的認證與授權，並使用 common 模塊中的 JWT 配置 進行身份驗證。