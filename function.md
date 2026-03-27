# Auth & Authorization Service — Danh sách chức năng

> **Stack:** Spring Boot · Spring Security · JWT · PostgreSQL · Redis · Kafka/RabbitMQ  
> **Mục tiêu:** Phục vụ kiến trúc microservice cho mạng xã hội

---

## Mục lục

1. [Authentication — Xác thực người dùng](#1-authentication--xác-thực-người-dùng)
2. [Token Management — Quản lý token](#2-token-management--quản-lý-token)
3. [Authorization — Phân quyền](#3-authorization--phân-quyền)
4. [Social Login & OAuth2](#4-social-login--oauth2)
5. [Multi-Factor Authentication (MFA)](#5-multi-factor-authentication-mfa)
6. [User Info & Profile cơ bản](#6-user-info--profile-cơ-bản)
7. [Admin Management](#7-admin-management)
8. [Service-to-Service Communication](#8-service-to-service-communication)
9. [Security & Non-functional](#9-security--non-functional)
10. [Storage & Infrastructure](#10-storage--infrastructure)

---

## 1. Authentication — Xác thực người dùng

### 1.1 Đăng ký tài khoản
- **Endpoint:** `POST /api/v1/auth/register`
- **Chức năng:**
    - Nhận `email`, `username`, `password`, `fullName`
    - Validate: email hợp lệ, username không trùng, password đủ mạnh (min 8 ký tự, có số, có chữ hoa)
    - Hash password bằng **BCrypt** (strength = 12)
    - Lưu user với trạng thái `UNVERIFIED`
    - Gán role mặc định `ROLE_USER`
    - Gửi email xác thực (publish event hoặc gọi trực tiếp Email Service)
- **Response:** `201 Created` — thông báo kiểm tra email

### 1.2 Đăng nhập
- **Endpoint:** `POST /api/v1/auth/login`
- **Chức năng:**
    - Nhận `email/username` + `password`
    - Kiểm tra user tồn tại, tài khoản chưa bị khoá (`ACTIVE`)
    - Kiểm tra email đã xác thực chưa
    - Xác thực password với BCrypt
    - Tạo **Access Token** (JWT, TTL 15 phút)
    - Tạo **Refresh Token** (opaque hoặc JWT, TTL 7 ngày), lưu vào Redis
    - Ghi nhận thông tin đăng nhập: IP, User-Agent, timestamp
    - Trả Access Token trong body, Refresh Token qua `HttpOnly cookie`
- **Bảo mật:** Rate limiting — tối đa 5 lần thất bại / 15 phút / IP

### 1.3 Đăng xuất
- **Endpoint:** `POST /api/v1/auth/logout`
- **Chức năng:**
    - Xoá Refresh Token khỏi Redis
    - Blacklist Access Token hiện tại (lưu `jti` vào Redis với TTL = thời gian còn lại của token)
    - Xoá `HttpOnly cookie`
- **Yêu cầu:** Bearer token hợp lệ

### 1.4 Đăng xuất khỏi tất cả thiết bị
- **Endpoint:** `POST /api/v1/auth/logout-all`
- **Chức năng:**
    - Xoá toàn bộ Refresh Token của user trong Redis
    - Tăng `tokenVersion` của user trong DB (invalidate tất cả JWT đang active)

### 1.5 Làm mới Access Token
- **Endpoint:** `POST /api/v1/auth/refresh-token`
- **Chức năng:**
    - Nhận Refresh Token từ `HttpOnly cookie` hoặc request body
    - Kiểm tra token trong Redis (còn tồn tại, chưa bị thu hồi)
    - Verify chữ ký và thời hạn
    - Tạo Access Token mới
    - **Rotate Refresh Token** — tạo token mới, xoá token cũ (phòng replay attack)
    - Phát hiện Refresh Token reuse: nếu token đã bị xoá mà vẫn dùng → thu hồi toàn bộ session của user

### 1.6 Quên mật khẩu
- **Endpoint:** `POST /api/v1/auth/forgot-password`
- **Chức năng:**
    - Nhận `email`
    - Tạo reset token (UUID hoặc JWT ngắn hạn, TTL 15 phút), lưu Redis
    - Gửi email chứa link reset password
    - **Luôn trả `200 OK`** dù email có tồn tại hay không (tránh user enumeration)

### 1.7 Đặt lại mật khẩu
- **Endpoint:** `POST /api/v1/auth/reset-password`
- **Chức năng:**
    - Nhận `token` + `newPassword` + `confirmPassword`
    - Validate token còn hạn trong Redis
    - Hash và cập nhật password mới
    - Thu hồi toàn bộ session hiện tại của user
    - Xoá reset token khỏi Redis

### 1.8 Xác thực email
- **Endpoint:** `GET /api/v1/auth/verify-email?token={token}`
- **Chức năng:**
    - Validate token (lưu trong Redis, TTL 24 giờ)
    - Cập nhật trạng thái user từ `UNVERIFIED` → `ACTIVE`
    - Xoá token khỏi Redis

### 1.9 Gửi lại email xác thực
- **Endpoint:** `POST /api/v1/auth/resend-verification`
- **Chức năng:**
    - Rate limit: 1 lần / 2 phút / email
    - Tạo token mới, gửi lại email

### 1.10 Đổi mật khẩu (đã đăng nhập)
- **Endpoint:** `PUT /api/v1/auth/change-password`
- **Chức năng:**
    - Nhận `currentPassword` + `newPassword`
    - Xác thực mật khẩu hiện tại
    - Hash và cập nhật mật khẩu mới
    - Thu hồi toàn bộ session khác (giữ session hiện tại)

---

## 2. Token Management — Quản lý token

### 2.1 Validate token (dành cho API Gateway / các service khác)
- **Endpoint:** `POST /api/v1/auth/token/validate`
- **Chức năng:**
    - Verify chữ ký JWT
    - Kiểm tra token chưa hết hạn
    - Kiểm tra `jti` không nằm trong blacklist (Redis)
    - Kiểm tra `tokenVersion` khớp với user trong DB
    - Trả về `{ valid: true/false, userId, roles, permissions }`
- **Lưu ý:** Endpoint này nên được cache phía Gateway để giảm tải

### 2.2 Token introspection
- **Endpoint:** `POST /api/v1/auth/token/introspect`
- **Chức năng:**
    - Trả về đầy đủ thông tin claims của token
    - Dành cho các service cần thông tin chi tiết hơn
    - Response: `{ active, userId, email, username, roles, permissions, exp, iat, jti }`

### 2.3 Thu hồi token
- **Endpoint:** `POST /api/v1/auth/token/revoke`
- **Chức năng:**
    - Thu hồi một Refresh Token cụ thể (đăng xuất một thiết bị)
    - Yêu cầu user phải là chủ của token

### 2.4 Lấy danh sách session đang hoạt động
- **Endpoint:** `GET /api/v1/auth/sessions`
- **Chức năng:**
    - Trả danh sách các Refresh Token đang active của user hiện tại
    - Hiển thị: thiết bị, IP, thời gian đăng nhập, lần cuối dùng

### 2.5 Public key endpoint (JWKS)
- **Endpoint:** `GET /.well-known/jwks.json`
- **Chức năng:**
    - Trả về public key theo chuẩn **JSON Web Key Set (JWKS)**
    - Cho phép API Gateway và các service tự verify JWT **mà không cần gọi Auth Service** mỗi request
    - Cache `Cache-Control: max-age=3600`
    - Hỗ trợ key rotation (giữ key cũ trong JWKS khi đang trong giai đoạn chuyển đổi)

### 2.6 Cấu trúc JWT Claims

```json
{
  "sub": "user-uuid",
  "jti": "unique-token-id",
  "iat": 1700000000,
  "exp": 1700000900,
  "tokenVersion": 3,
  "email": "user@example.com",
  "username": "johndoe",
  "roles": ["ROLE_USER"],
  "permissions": ["POST_CREATE", "COMMENT_CREATE"],
  "status": "ACTIVE"
}
```

---

## 3. Authorization — Phân quyền

### 3.1 Kiểm tra quyền truy cập
- **Endpoint:** `POST /api/v1/auth/authorize`
- **Request body:** `{ userId, resource, action }`
    - Ví dụ: `{ userId: "abc", resource: "post:123", action: "DELETE" }`
- **Chức năng:**
    - Kiểm tra user có role/permission phù hợp không
    - Hỗ trợ kiểm tra resource-level (xem user có phải chủ resource không)
    - Trả `{ allowed: true/false, reason }`

### 3.2 Quản lý Role
- `GET    /api/v1/roles` — Lấy danh sách role
- `POST   /api/v1/roles` — Tạo role mới *(Admin)*
- `PUT    /api/v1/roles/{roleId}` — Cập nhật role *(Admin)*
- `DELETE /api/v1/roles/{roleId}` — Xoá role *(Admin)*
- `GET    /api/v1/roles/{roleId}/permissions` — Xem permissions của role

### 3.3 Quản lý Permission
- `GET    /api/v1/permissions` — Lấy danh sách tất cả permissions
- `POST   /api/v1/permissions` — Tạo permission mới *(Admin)*
- `POST   /api/v1/roles/{roleId}/permissions` — Gán permission cho role *(Admin)*
- `DELETE /api/v1/roles/{roleId}/permissions/{permId}` — Gỡ permission *(Admin)*

### 3.4 Gán Role cho User
- `GET    /api/v1/users/{userId}/roles` — Xem roles của user
- `POST   /api/v1/users/{userId}/roles` — Gán role cho user *(Admin)*
- `DELETE /api/v1/users/{userId}/roles/{roleId}` — Gỡ role khỏi user *(Admin)*

### 3.5 Thiết kế RBAC

```
User ──< UserRole >── Role ──< RolePermission >── Permission
```

| Bảng | Mô tả |
|------|-------|
| `users` | Thông tin tài khoản |
| `roles` | `ROLE_USER`, `ROLE_MODERATOR`, `ROLE_ADMIN` |
| `permissions` | `POST_CREATE`, `POST_DELETE_ANY`, `USER_BAN`, ... |
| `user_roles` | Liên kết user ↔ role |
| `role_permissions` | Liên kết role ↔ permission |

---

## 4. Social Login & OAuth2

### 4.1 Khởi tạo OAuth2 flow
- **Endpoint:** `GET /api/v1/auth/oauth2/{provider}`
    - `provider`: `google`, `facebook`, `github`
- **Chức năng:**
    - Tạo `state` token (chống CSRF), lưu Redis TTL 10 phút
    - Redirect đến trang đăng nhập của provider

### 4.2 OAuth2 Callback
- **Endpoint:** `GET /api/v1/auth/oauth2/callback/{provider}`
- **Chức năng:**
    - Nhận `code` và `state` từ provider
    - Validate `state` token
    - Exchange `code` → access token của provider
    - Lấy thông tin user từ provider (email, name, avatar, providerId)
    - **Nếu email đã tồn tại:** liên kết tài khoản social với tài khoản hiện tại
    - **Nếu chưa tồn tại:** tạo tài khoản mới (email đã xác thực, không cần password)
    - Lưu vào bảng `oauth_accounts` (`userId`, `provider`, `providerAccountId`, `accessToken`)
    - Tạo JWT và trả về như đăng nhập thông thường

### 4.3 Liên kết / Huỷ liên kết social account
- `GET    /api/v1/auth/oauth2/connected` — Xem các tài khoản social đã liên kết
- `DELETE /api/v1/auth/oauth2/{provider}` — Huỷ liên kết (chỉ khi user có password hoặc còn provider khác)

---

## 5. Multi-Factor Authentication (MFA)

### 5.1 Bật MFA (TOTP)
- **Endpoint:** `POST /api/v1/auth/mfa/setup`
- **Chức năng:**
    - Tạo TOTP secret (dùng thư viện `google-auth-library` hoặc `GoogleAuthenticator`)
    - Trả về QR code URL và backup codes
    - MFA chưa active cho đến khi verify lần đầu

### 5.2 Xác nhận bật MFA
- **Endpoint:** `POST /api/v1/auth/mfa/verify-setup`
- **Chức năng:**
    - Nhận `code` TOTP từ app
    - Kích hoạt MFA cho tài khoản
    - Lưu secret (đã mã hoá) vào DB

### 5.3 Tắt MFA
- **Endpoint:** `DELETE /api/v1/auth/mfa/disable`
- **Chức năng:**
    - Yêu cầu xác thực lại bằng password + mã TOTP hiện tại
    - Xoá secret khỏi DB

### 5.4 Xác thực khi đăng nhập với MFA
- **Endpoint:** `POST /api/v1/auth/mfa/challenge`
- **Chức năng:**
    - Sau bước đăng nhập thành công → nếu user có MFA, trả về `mfaRequired: true` + `tempToken`
    - User submit `tempToken` + `code` TOTP
    - Validate TOTP (cho phép lệch ±1 bước = ±30 giây)
    - Trả JWT đầy đủ

### 5.5 Backup codes
- `GET  /api/v1/auth/mfa/backup-codes` — Xem backup codes còn lại
- `POST /api/v1/auth/mfa/backup-codes/regenerate` — Tạo mới backup codes (thu hồi toàn bộ cũ)
- `POST /api/v1/auth/mfa/backup-codes/use` — Sử dụng một backup code để đăng nhập

---

## 6. User Info & Profile cơ bản

> Auth Service chỉ quản lý thông tin xác thực. Profile đầy đủ (bio, avatar, followers...) thuộc về **User/Profile Service**.

### 6.1 Thông tin user hiện tại
- **Endpoint:** `GET /api/v1/users/me`
- **Response:** `userId`, `email`, `username`, `fullName`, `status`, `roles`, `mfaEnabled`, `createdAt`

### 6.2 Cập nhật thông tin cơ bản
- **Endpoint:** `PUT /api/v1/users/me`
- **Chức năng:** Cập nhật `username`, `fullName`
- **Validate:** Username không trùng với user khác

### 6.3 Lấy thông tin user theo ID (internal)
- **Endpoint:** `GET /api/v1/users/{userId}`
- **Chức năng:** Dành cho các service nội bộ lấy thông tin xác thực cơ bản
- **Bảo mật:** Chỉ trả về trường an toàn (`userId`, `username`, `email`, `status`, `roles`)

---

## 7. Admin Management

### 7.1 Danh sách người dùng
- **Endpoint:** `GET /api/v1/admin/users`
- **Query params:** `page`, `size`, `sort`, `status`, `role`, `search`
- **Yêu cầu:** `ROLE_ADMIN`

### 7.2 Khoá / Mở khoá tài khoản
- **Endpoint:** `PUT /api/v1/admin/users/{userId}/status`
- **Body:** `{ status: "BANNED" | "ACTIVE" | "SUSPENDED", reason }`
- **Chức năng:**
    - Cập nhật trạng thái user
    - Khi ban: thu hồi toàn bộ token của user
    - Publish event `user.banned` / `user.activated`

### 7.3 Xem lịch sử đăng nhập
- **Endpoint:** `GET /api/v1/admin/users/{userId}/login-history`
- **Thông tin:** IP, thiết bị, thời gian, thành công/thất bại

### 7.4 Xoá tài khoản
- **Endpoint:** `DELETE /api/v1/admin/users/{userId}`
- **Chức năng:** Soft delete — đánh dấu `DELETED`, anonymize PII, publish event `user.deleted`

---

## 8. Service-to-Service Communication

> Các endpoint này **không expose ra ngoài internet**, chỉ accessible trong internal network / service mesh.

### 8.1 Validate token nhanh (internal)
- **Endpoint:** `POST /internal/v1/token/validate`
- **Bảo mật:** API Key trong header `X-Internal-Api-Key` hoặc mTLS
- **Chức năng:** Tương tự 2.1 nhưng được tối ưu, có thể cache trong Gateway

### 8.2 Lấy thông tin user (internal)
- **Endpoint:** `GET /internal/v1/users/{userId}`
- **Dùng bởi:** Post Service, Comment Service, Notification Service...
- **Response:** `{ userId, username, displayName, avatarUrl, status, roles }`

### 8.3 Lấy nhiều user cùng lúc (batch)
- **Endpoint:** `POST /internal/v1/users/batch`
- **Body:** `{ userIds: ["id1", "id2", ...] }`
- **Dùng bởi:** Feed Service khi cần render danh sách tác giả

### 8.4 Service token (M2M)
- **Endpoint:** `POST /internal/v1/service-token`
- **Chức năng:**
    - Cấp JWT ngắn hạn cho một service gọi sang service khác
    - Input: `clientId` + `clientSecret` (đã đăng ký)
    - Trả token với claim `serviceId`, không có `userId`

### 8.5 Domain Events (Message Broker)

| Event | Topic | Trigger |
|-------|-------|---------|
| `user.registered` | `auth.user.registered` | Đăng ký thành công |
| `user.verified` | `auth.user.verified` | Xác thực email |
| `user.logged-in` | `auth.user.logged-in` | Đăng nhập thành công |
| `user.password-changed` | `auth.user.password-changed` | Đổi/reset mật khẩu |
| `user.role-changed` | `auth.user.role-changed` | Thay đổi role |
| `user.banned` | `auth.user.banned` | Tài khoản bị khoá |
| `user.deleted` | `auth.user.deleted` | Xoá tài khoản |

**Các service subscribe:** Notification Service (gửi email), User/Profile Service (đồng bộ dữ liệu), Audit Service (ghi log).

---

## 9. Security & Non-functional

### 9.1 Bảo mật API
- [ ] HTTPS/TLS bắt buộc trên tất cả endpoint
- [ ] CORS cấu hình whitelist domain
- [ ] Helmet headers: `X-Content-Type-Options`, `X-Frame-Options`, `Strict-Transport-Security`
- [ ] `HttpOnly`, `Secure`, `SameSite=Strict` cho cookie chứa Refresh Token

### 9.2 Rate Limiting

| Endpoint | Giới hạn |
|----------|----------|
| `POST /auth/login` | 5 lần / 15 phút / IP |
| `POST /auth/register` | 3 lần / giờ / IP |
| `POST /auth/forgot-password` | 3 lần / giờ / email |
| `POST /auth/resend-verification` | 1 lần / 2 phút / email |
| `POST /auth/mfa/challenge` | 5 lần / 15 phút / user |

### 9.3 Audit Logging
- Ghi log mọi sự kiện xác thực quan trọng: đăng nhập, đổi mật khẩu, thay đổi role, ban user
- Trường cần log: `timestamp`, `userId`, `action`, `ip`, `userAgent`, `success`, `failReason`
- Lưu vào DB hoặc gửi sang Audit Service

### 9.4 Account Lockout
- Sau **5 lần đăng nhập sai** → khoá tạm 15 phút (lưu trạng thái trong Redis)
- Sau **20 lần sai trong ngày** → khoá vĩnh viễn, yêu cầu liên hệ admin

### 9.5 Phát hiện bất thường
- [ ] Đăng nhập từ IP mới / quốc gia mới → gửi email thông báo
- [ ] Đăng nhập đồng thời từ nhiều IP khác nhau → flag để review
- [ ] Refresh Token được dùng sau khi đã rotate → revoke toàn bộ session (token theft detection)

### 9.6 Mã hoá & Bảo mật dữ liệu
- Password: BCrypt với cost factor 12
- TOTP secret: mã hoá AES-256 trước khi lưu DB
- Refresh Token: lưu dạng hash (SHA-256) trong Redis, không lưu plaintext
- PII (email, phone): cân nhắc mã hoá tại tầng application

---

## 10. Storage & Infrastructure

### 10.1 PostgreSQL — Schema chính

```sql
-- Bảng users
users (
  id            UUID PRIMARY KEY,
  email         VARCHAR(255) UNIQUE NOT NULL,
  username      VARCHAR(50) UNIQUE NOT NULL,
  password_hash VARCHAR(255),            -- NULL nếu chỉ dùng social login
  full_name     VARCHAR(255),
  status        ENUM('UNVERIFIED','ACTIVE','SUSPENDED','BANNED','DELETED'),
  token_version INT DEFAULT 0,           -- Tăng khi muốn invalidate tất cả JWT
  mfa_enabled   BOOLEAN DEFAULT FALSE,
  mfa_secret    VARCHAR(255),            -- Mã hoá AES-256
  created_at    TIMESTAMP,
  updated_at    TIMESTAMP,
  last_login_at TIMESTAMP
)

-- Bảng oauth_accounts
oauth_accounts (
  id                  UUID PRIMARY KEY,
  user_id             UUID REFERENCES users(id),
  provider            VARCHAR(50),       -- google, facebook, github
  provider_account_id VARCHAR(255),
  access_token        TEXT,
  refresh_token       TEXT,
  token_expires_at    TIMESTAMP,
  UNIQUE(provider, provider_account_id)
)

-- Bảng roles, permissions, user_roles, role_permissions
roles (id, name, description)
permissions (id, name, description, resource)
user_roles (user_id, role_id, granted_at, granted_by)
role_permissions (role_id, permission_id)

-- Bảng login_history
login_history (
  id         UUID PRIMARY KEY,
  user_id    UUID,
  ip_address VARCHAR(45),
  user_agent TEXT,
  success    BOOLEAN,
  fail_reason VARCHAR(100),
  created_at TIMESTAMP
)
```

### 10.2 Redis — Key patterns

| Key | Giá trị | TTL |
|-----|---------|-----|
| `refresh_token:{hash}` | `userId` | 7 ngày |
| `blacklist:{jti}` | `"1"` | Còn lại của access token |
| `reset_password:{token}` | `userId` | 15 phút |
| `email_verify:{token}` | `userId` | 24 giờ |
| `mfa_temp:{tempToken}` | `userId` | 5 phút |
| `login_attempts:{ip}` | số lần thất bại | 15 phút |
| `rate_limit:forgot:{email}` | số lần gọi | 1 giờ |
| `oauth_state:{state}` | `redirectUrl` | 10 phút |
| `service_sessions:{userId}` | Set of session IDs | — |

### 10.3 Cấu trúc module Spring Boot đề xuất

```
auth-service/
├── api/
│   ├── AuthController.java
│   ├── TokenController.java
│   ├── UserController.java
│   ├── AdminController.java
│   ├── OAuth2Controller.java
│   └── internal/
│       └── InternalAuthController.java
├── domain/
│   ├── model/          (User, Role, Permission, OAuthAccount)
│   ├── repository/     (UserRepository, RoleRepository...)
│   └── event/          (UserRegisteredEvent, UserBannedEvent...)
├── application/
│   ├── AuthService.java
│   ├── TokenService.java
│   ├── OAuth2Service.java
│   ├── MfaService.java
│   └── UserService.java
├── infrastructure/
│   ├── security/       (JwtFilter, SecurityConfig, JwtProvider)
│   ├── redis/          (TokenStore, RateLimiter)
│   ├── messaging/      (EventPublisher)
│   └── oauth2/         (GoogleOAuth2Client, FacebookOAuth2Client)
└── config/
    ├── SecurityConfig.java
    └── RedisConfig.java
```

---

## Checklist tích hợp với các service khác

### API Gateway tích hợp

- [ ] Gateway lấy JWKS từ `/.well-known/jwks.json` và cache
- [ ] Gateway verify JWT local, không gọi Auth Service mỗi request
- [ ] Sau verify, inject header: `X-User-Id`, `X-User-Roles`, `X-User-Email`
- [ ] Gateway forward các header này đến downstream service
- [ ] Với endpoint public (không cần auth): bypass JWT check

### Các service downstream nhận gì

Mỗi request đến downstream service sẽ có:
```
X-User-Id:    550e8400-e29b-41d4-a716-446655440000
X-User-Roles: ROLE_USER,ROLE_MODERATOR
X-User-Email: user@example.com
```

Downstream service **không cần gọi Auth Service** — tin tưởng vào header từ Gateway (vì Gateway đã verify).

### Các service cần subscribe events

| Service | Event cần subscribe |
|---------|---------------------|
| User/Profile Service | `user.registered`, `user.deleted` |
| Notification Service | `user.registered`, `user.verified`, `user.banned` |
| Post/Content Service | `user.banned`, `user.deleted` |
| Search Service | `user.registered`, `user.deleted` |
| Audit/Log Service | Tất cả events |
