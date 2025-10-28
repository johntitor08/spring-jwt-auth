# 🔐 JWT Authentication Microservice

<https://img.shields.io/badge/Spring%2520Boot-3.2.0-brightgreen>
<https://img.shields.io/badge/Security-JWT%2520%252B%2520Cookies-blue>
<https://img.shields.io/badge/Database-H2-informational>

A robust, production-ready JWT authentication microservice with HTTP-only cookie refresh tokens and comprehensive security features.

🚀 Features
JWT Access Tokens - Stateless authentication with 1-hour expiration

HTTP-only Cookie Refresh Tokens - Secure automatic token refresh (7-day expiration)

Spring Security 6 - Latest security framework with stateless sessions

H2 Database - In-memory database with web console

CORS Configured - Ready for frontend integration

Complete API - Register, login, refresh, logout, and protected endpoints

Production Ready - Proper error handling, transaction management, and security best practices

🛠 Tech Stack
Java 17+

Spring Boot 3.2.0

Spring Security 6

Spring Data JPA

H2 Database (In-memory)

JJWT (JSON Web Tokens)

Maven

📦 Installation & Setup
Prerequisites
Java 17 or higher

Maven 3.6+

## Complete Postman Collection JSON

json
{
  "info": {
    "name": "JWT Auth Service",
    "description": "Complete JWT authentication with cookie-based refresh tokens",
    "schema": "<https://schema.getpostman.com/json/collection/v2.1.0/collection.json>"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080",
      "type": "string"
    },
    {
      "key": "accessToken",
      "value": "",
      "type": "string"
    },
    {
      "key": "username",
      "value": "testuser",
      "type": "string"
    },
    {
      "key": "password",
      "value": "testpass123",
      "type": "string"
    }
  ],
  "item": [
    {
      "name": "01 - Health Check",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/auth/health",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "health"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});",
              "pm.test(\"Response should contain running\", function() {",
              "    pm.response.to.have.body(\"Auth service is running ✅\");",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "02 - Register User",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"{{username}}\",\n  \"password\": \"{{password}}\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/register",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "register"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});",
              "pm.test(\"Registration successful\", function() {",
              "    pm.expect(pm.response.text()).to.include(\"successfully\");",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "03 - Login (Sets HTTP-only Cookie)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"{{username}}\",\n  \"password\": \"{{password}}\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/login",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "login"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});",
              "pm.test(\"Access token received\", function() {",
              "    var jsonData = pm.response.json();",
              "    pm.environment.set(\"accessToken\", jsonData.accessToken);",
              "    pm.expect(jsonData.accessToken).to.not.be.empty;",
              "    pm.expect(jsonData.refreshToken).to.be.undefined;",
              "});",
              "pm.test(\"Refresh token cookie set\", function() {",
              "    var cookies = pm.cookies.getAll();",
              "    var refreshTokenCookie = cookies.find(cookie => cookie.name === 'refreshToken');",
              "    pm.expect(refreshTokenCookie).to.not.be.undefined;",
              "    pm.expect(refreshTokenCookie.value).to.not.be.empty;",
              "    console.log('Refresh Token Cookie:', refreshTokenCookie);",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "04 - Access Protected Endpoint",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{accessToken}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/auth/protected",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "protected"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});",
              "pm.test(\"Protected endpoint accessible\", function() {",
              "    pm.expect(pm.response.text()).to.include(\"token ile erişilebilir\");",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "05 - Validate Token",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/auth/validate",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "validate"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "06 - Refresh Token (Uses Cookie)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/refresh",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "refresh"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});",
              "pm.test(\"New access token received\", function() {",
              "    var jsonData = pm.response.json();",
              "    var oldToken = pm.environment.get(\"accessToken\");",
              "    pm.environment.set(\"accessToken\", jsonData.accessToken);",
              "    pm.expect(jsonData.accessToken).to.not.be.empty;",
              "    pm.expect(jsonData.accessToken).to.not.equal(oldToken);",
              "    pm.expect(jsonData.refreshToken).to.be.undefined;",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "07 - Access Protected with New Token",
      "request": {
        "method": "GET",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{accessToken}}"
          }
        ],
        "url": {
          "raw": "{{baseUrl}}/auth/protected",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "protected"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "08 - Access Without Token (Should Fail)",
      "request": {
        "method": "GET",
        "header": [],
        "url": {
          "raw": "{{baseUrl}}/auth/protected",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "protected"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 401\", function() {",
              "    pm.response.to.have.status(401);",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "09 - Logout (Clears Cookie)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/logout",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "logout"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 200\", function() {",
              "    pm.response.to.have.status(200);",
              "});",
              "pm.test(\"Refresh token cookie cleared\", function() {",
              "    var cookies = pm.cookies.getAll();",
              "    var refreshTokenCookie = cookies.find(cookie => cookie.name === 'refreshToken');",
              "    pm.expect(refreshTokenCookie).to.be.undefined;",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "10 - Try Refresh After Logout (Should Fail)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/refresh",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "refresh"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 400\", function() {",
              "    pm.response.to.have.status(400);",
              "});"
            ]
          }
        }
      ]
    },
    {
      "name": "11 - Register Duplicate User (Should Fail)",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Content-Type",
            "value": "application/json"
          }
        ],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"username\": \"{{username}}\",\n  \"password\": \"{{password}}\"\n}"
        },
        "url": {
          "raw": "{{baseUrl}}/auth/register",
          "host": ["{{baseUrl}}"],
          "path": ["auth", "register"]
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "exec": [
              "pm.test(\"Status should be 400\", function() {",
              "    pm.response.to.have.status(400);",
              "});",
              "pm.test(\"Duplicate user error\", function() {",
              "    pm.expect(pm.response.text()).to.include(\"already exists\");",
              "});"
            ]
          }
        }
      ]
    }
  ]
}

## Testing Sequence

Run these requests in order for complete testing:

Health Check - Verify service is running

Register - Create test user

Login - Get access token + refresh token cookie

Access Protected - Test JWT authentication

Validate Token - Check token validity

Refresh Token - Get new tokens using cookie

Access Protected with New Token - Verify new token works

Access Without Token - Test security (should fail)

Logout - Clear authentication

Try Refresh After Logout - Verify logout worked (should fail)

Register Duplicate - Test error handling

## application.properties

## Application

spring.application.name=auth
server.port=8080

## Database

spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

## JPA

spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

## H2 Console

spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

## JWT

jwt.secret=supersecretkey1234567890123456789012345678901234567890
jwt.expiration=3600000

## Logging

logging.level.com.example.demo=DEBUG
logging.level.org.springframework.security=DEBUG

## Transaction

spring.transaction.default-timeout=30s
Security Configuration
CORS: Configured for <http://localhost:3000> (React/Vue frontend)

CSRF: Disabled (stateless JWT authentication)

Session Management: Stateless

Password Encoding: BCrypt

🏗 Project Structure
text
src/main/java/com/example/demo/
├── controller/
│   └── AuthController.java          # REST API endpoints
├── service/
│   └── AuthService.java             # Business logic
├── security/
│   ├── SecurityConfig.java          # Security configuration
│   ├── AppUserDetailsService.java   # User details service
│   └── jwt/
│       ├── JwtAuthFilter.java       # JWT authentication filter
│       └── JwtService.java          # JWT token operations
├── repository/
│   ├── UserRepository.java          # User data access
│   └── RefreshTokenRepository.java  # Refresh token management
├── model/
│   ├── UserEntity.java              # User entity
│   └── RefreshToken.java            # Refresh token entity
└── dto/
    ├── AuthRequest.java             # Login/Register request
    ├── AuthResponse.java            # Authentication response
    └── RefreshTokenRequest.java     # Refresh token request

## 🔒 Security Features

JWT Security
HS256 Algorithm - Secure signing algorithm

1-Hour Expiration - Short-lived access tokens

Subject Claims - Username-based token validation

Automatic Validation - Spring Security integration

Refresh Token Security
HTTP-only Cookies - Prevents XSS attacks

7-Day Expiration - Long-lived but secure

Automatic Rotation - New refresh token on each use

Database Storage - Server-side validation

Additional Security
BCrypt Password Hashing - Secure password storage

CORS Configuration - Controlled cross-origin requests

Stateless Sessions - No server-side session storage

Transaction Safety - Optimistic locking to prevent conflicts

## 🚨 Error Handling

The service provides meaningful error responses:

json
{
  "timestamp": "2024-01-15T10:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid refresh token",
  "path": "/auth/refresh"
}

### Common error scenarios

400 Bad Request - Invalid input, missing tokens

401 Unauthorized - Invalid or expired JWT

500 Internal Server Error - Server-side issues

## 🔄 Token Flow

text

1. User Login
   ↓
2. Generate JWT Access Token (1 hour) + HTTP-only Refresh Token Cookie (7 days)
   ↓
3. Client stores JWT in memory/localStorage
   ↓
4. Client includes JWT in Authorization header for API calls
   ↓
5. When JWT expires, client calls /auth/refresh (automatically sends cookie)
   ↓
6. Server validates refresh token, issues new JWT + new refresh token cookie
   ↓
7. Repeat steps 4-6 until logout
   ↓
8. On logout, refresh token cookie is cleared and invalidated

### Clean build

./mvnw clean compile

### Run tests

./mvnw test

### Create executable JAR

./mvnw package

## 🚀 Deployment

Production Considerations
Update JWT Secret: Change jwt.secret to a secure random string

Enable HTTPS: Set app.cookie.secure=true in production

Database: Switch from H2 to PostgreSQL/MySQL

Logging: Configure production logging levels

Monitoring: Add health checks and metrics

## Docker Deployment

dockerfile
FROM openjdk:17-jdk-slim
COPY target/auth-service-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]

## 🤝 Contributing

Fork the repository

Create a feature branch: git checkout -b feature/amazing-feature

Commit changes: git commit -m 'Add amazing feature'

Push to branch: git push origin feature/amazing-feature

Open a Pull Request

### 🆘 Troubleshooting

Common Issues:

JWT Validation Fails

Check JWT secret matches between restarts

Verify token expiration settings

Cookie Not Set

Ensure CORS is properly configured for your frontend domain

Check that allowCredentials is set to true

Transaction Errors

Verify H2 database is properly configured

Check entity versioning for optimistic locking

H2 Console Not Accessible

Verify spring.h2.console.enabled=true

Check that security configuration allows /h2-console/**

## 📞 Support

For support and questions:

Create an issue in the repository

Check existing issues for solutions

Review Spring Security documentation

Built with ❤️ using Spring Boot and Spring Security

Ready to secure your applications! 🚀
