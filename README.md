# jwt-auth-api

A JWT authentication REST API built with Spring Boot 3.5 and MySQL. Access + refresh token pair, role-based access control, Swagger documentation, Dockerized for one-command setup.

Written because every Spring Boot tutorial online stops at `/login` — this one includes proper token refresh, global exception handling, standardized response envelope, role-based endpoint protection, and tests.

## What it does

- Signup, login, token refresh, current-user profile
- BCrypt password hashing
- Access tokens (15 min) + refresh tokens (7 days), distinguishable via `type` claim
- Role-based access with `@PreAuthorize` — `ROLE_USER`, `ROLE_ADMIN`
- Every response wrapped in a consistent `ApiResponse<T>` envelope (success, error, validation, paginated)
- Request validation with Jakarta constraints — 400 responses include per-field error details
- Stateless (no server session) — horizontally scalable
- Swagger UI with Bearer auth scheme — try endpoints directly from browser

## Tech stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security 6, jjwt 0.12 |
| Persistence | Spring Data JPA, Hibernate |
| Database | MySQL 8 (H2 for tests) |
| API docs | springdoc-openapi 2.6 |
| Build | Maven (wrapper included — no local Maven needed) |
| Container | Docker + docker-compose |

## Quick start

### With Docker (recommended)

```bash
git clone https://github.com/himanshu-upadhyay-dev/jwt-auth-api.git
cd jwt-auth-api
cp .env.example .env                # edit JWT_SECRET before running in prod
docker compose up -d --build
```

- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- MySQL: `localhost:3307` (mapped — keeps host MySQL free)

### Without Docker

Requires Java 17+ and a running MySQL 8 instance.

```bash
export DB_URL="jdbc:mysql://localhost:3306/jwt_auth_db?createDatabaseIfNotExist=true"
export DB_USERNAME=root
export DB_PASSWORD=your_password
export JWT_SECRET=your-long-random-256-bit-secret

./mvnw spring-boot:run
```

On Windows PowerShell, use `$env:DB_URL="..."` instead of `export`.

## API reference

| Method | Path | Auth | Purpose |
|---|---|---|---|
| POST | `/api/v1/auth/signup` | Public | Register a new user |
| POST | `/api/v1/auth/login` | Public | Authenticate, returns token pair |
| POST | `/api/v1/auth/refresh` | Public | Issue new token pair from refresh token |
| GET | `/api/v1/auth/me` | Bearer | Profile of the current user |
| GET | `/api/v1/test/user` | ROLE_USER | Sample user-only endpoint |
| GET | `/api/v1/test/admin` | ROLE_ADMIN | Sample admin-only endpoint |

Full schema in Swagger UI at `/swagger-ui.html`.

## Example walkthrough

```bash
# 1. Register
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "himanshu",
    "email": "himanshu@example.com",
    "password": "Str0ngPass1",
    "fullName": "Himanshu Upadhyay"
  }'

# 2. Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"usernameOrEmail":"himanshu","password":"Str0ngPass1"}'

# Response (shortened):
# {
#   "success": true,
#   "data": {
#     "accessToken": "eyJhbGciOi...",
#     "refreshToken": "eyJhbGciOi...",
#     "tokenType": "Bearer",
#     "expiresInMs": 900000
#   }
# }

# 3. Call a protected endpoint
curl http://localhost:8080/api/v1/auth/me \
  -H "Authorization: Bearer <accessToken>"
```

## Response envelope

Every response — success or failure — follows the same shape:

```json
{
  "success": true,
  "message": "Login successful",
  "httpStatus": "OK",
  "httpStatusCode": 200,
  "data": { ... },
  "apiVersion": "v1",
  "timestamp": "2026-04-19T12:34:56Z"
}
```

Error responses add `code` (stable machine-readable string), and validation errors include `errors[]`:

```json
{
  "success": false,
  "message": "Request validation failed",
  "httpStatusCode": 400,
  "code": "VALIDATION_FAILED",
  "errors": [
    { "field": "email", "message": "Email must be a valid email address", "rejectedValue": "not-an-email" }
  ]
}
```

Frontend can branch on `success` and handle `code` without parsing message strings.

## Configuration

All config is environment-driven. Defaults live in `application.yml`.

| Variable | Default | Notes |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/jwt_auth_db?...` | JDBC connection string |
| `DB_USERNAME` | `root` | MySQL username |
| `DB_PASSWORD` | `root` | MySQL password |
| `JWT_SECRET` | dev default | **Must override** in production — 256 bits minimum |
| `SERVER_PORT` | `8080` | HTTP port |

Token lifetimes (`access-token-expiration-ms`, `refresh-token-expiration-ms`) live in `application.yml` and can be overridden with `APP_JWT_ACCESS_TOKEN_EXPIRATION_MS` etc. via Spring's relaxed binding.

## Tests

```bash
./mvnw test
```

Tests run against H2 in-memory MySQL compatibility mode — no external database required. Coverage includes:

- `JwtServiceTest` — unit tests for token generation, parsing, validation, tamper detection
- `AuthServiceImplTest` — service-layer tests with mocked dependencies (Mockito)
- `AuthControllerIntegrationTest` — end-to-end HTTP tests via MockMvc: signup → login → `/me`, validation errors, duplicate users, unauthorized access, CORS preflight

## Project layout

```
src/main/java/com/upadhyay/jwtauth/
├── JwtAuthApiApplication.java
├── config/          AppProperties, SecurityConfig, OpenApiConfig
├── controller/      AuthController, TestController
├── dto/
│   ├── common/      ApiResponse, ErrorDetail, Pagination
│   ├── request/     SignupRequest, LoginRequest, RefreshTokenRequest
│   └── response/    AuthResponse, UserResponse
├── entity/          User, Role
├── exception/       BusinessException + 3 subclasses + GlobalExceptionHandler
├── repository/      UserRepository
├── security/        JwtService, JwtAuthenticationFilter, JwtAuthenticationEntryPoint,
│                    CustomUserDetails, CustomUserDetailsService
└── service/
    ├── AuthService.java (interface)
    └── impl/AuthServiceImpl.java
```

## Production checklist

Before deploying:

- Override `JWT_SECRET` with a strong random string (e.g. `openssl rand -base64 64`)
- Set `spring.jpa.hibernate.ddl-auto=validate` or `none` and manage schema via Flyway/Liquibase
- Tighten CORS — replace `setAllowedOrigins(List.of("*"))` with specific frontend origins
- Put the service behind HTTPS; never send JWTs over plain HTTP
- Consider rate-limiting `/login` and `/signup` (Bucket4j or gateway-level)

## License

MIT
