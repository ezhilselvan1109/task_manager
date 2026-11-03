# Package Structure Document: Task Management System

| Metadata Item | Details |
|---|---|
| **Document ID** | DOC-LLD-002 |
| **Document Version** | 1.0.0 |
| **Status** | Approved Baseline |
| **Author / Owner Role** | Software Architect |
| **Creation Date** | 2026-11-03 |
| **Related Documents** | `DOC-LLD-001`, `DOC-ARCH-002` |

---

## 1. Package Naming Conventions
All backend Java microservices follow standard Java reverse-domain package naming under base group `com.chellaa`:
- Base Group: `com.chellaa`
- Service Modules:
  - `com.chellaa.api_gateway`
  - `com.chellaa.user_service`
  - `com.chellaa.task_service`
  - `com.chellaa.notification_service`
  - `com.chellaa.service_discovery`

---

## 2. Microservice Package Structures

### 2.1 Task Service (`task-service`)
```
com.chellaa.task_service/
├── TaskServiceApplication.java
├── config/
│   ├── KafkaProducerConfig.java
│   ├── SecurityConfig.java
│   └── WebMvcConfig.java
├── context/
│   ├── UserContext.java
│   ├── UserContextFilter.java
│   └── UserContextHolder.java
├── controller/
│   ├── TaskController.java
│   └── TaskMetricsController.java
├── dto/
│   ├── request/
│   │   ├── CreateTaskRequest.java
│   │   ├── UpdateTaskRequest.java
│   │   ├── UpdateTaskPriorityRequest.java
│   │   └── UpdateTaskStatusRequest.java
│   └── response/
│       ├── PageResponse.java
│       ├── TaskMetricsResponse.java
│       └── TaskResponse.java
├── entity/
│   ├── OutboxEventEntity.java
│   ├── Priority.java (Enum)
│   ├── Status.java (Enum)
│   └── TaskEntity.java
├── event/
│   ├── TaskEventEnvelope.java
│   └── TaskEventPayload.java
├── exception/
│   ├── ErrorResponse.java
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── mapper/
│   └── TaskMapper.java
├── repository/
│   ├── OutboxEventRepository.java
│   └── TaskRepository.java
└── service/
    ├── TaskMetricsService.java
    ├── TaskService.java
    ├── impl/
    │   ├── TaskMetricsServiceImpl.java
    │   └── TaskServiceImpl.java
    └── outbox/
        ├── KafkaEventPublisher.java
        └── OutboxPollerService.java
```

### 2.2 User Service (`user-service`)
```
com.chellaa.user_service/
├── UserServiceApplication.java
├── config/
│   ├── JwtProperties.java
│   └── SecurityConfig.java
├── controller/
│   ├── AuthController.java
│   └── UserController.java
├── dto/
│   ├── request/
│   │   ├── LoginRequest.java
│   │   ├── RefreshTokenRequest.java
│   │   ├── RegisterRequest.java
│   │   └── UpdateProfileRequest.java
│   └── response/
│       ├── AuthResponse.java
│       └── UserProfileResponse.java
├── entity/
│   ├── RefreshTokenEntity.java
│   ├── UserEntity.java
│   └── UserStatus.java (Enum)
├── exception/
│   ├── EmailAlreadyExistsException.java
│   ├── GlobalExceptionHandler.java
│   └── InvalidCredentialsException.java
├── repository/
│   ├── RefreshTokenRepository.java
│   └── UserRepository.java
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtTokenProvider.java
│   └── PasswordEncoderConfig.java
└── service/
    ├── AuthService.java
    ├── UserService.java
    └── impl/
        ├── AuthServiceImpl.java
        └── UserServiceImpl.java
```

### 2.3 Notification Service (`notification-service`)
```
com.chellaa.notification_service/
├── NotificationServiceApplication.java
├── config/
│   ├── KafkaConsumerConfig.java
│   └── SecurityConfig.java
├── context/
│   ├── UserContextFilter.java
│   └── UserContextHolder.java
├── controller/
│   └── NotificationController.java
├── dto/
│   └── response/
│       ├── NotificationPageResponse.java
│       └── NotificationResponse.java
├── entity/
│   ├── ConsumedEventEntity.java
│   └── NotificationEntity.java
├── event/
│   ├── TaskEventEnvelope.java
│   └── TaskEventPayload.java
├── kafka/
│   ├── NotificationEventListener.java
│   └── NotificationErrorHandler.java
├── repository/
│   ├── ConsumedEventRepository.java
│   └── NotificationRepository.java
└── service/
    ├── NotificationService.java
    └── impl/
        └── NotificationServiceImpl.java
```

### 2.4 API Gateway (`api-gateway`)
```
com.chellaa.api_gateway/
├── ApiGatewayApplication.java
├── config/
│   ├── CorsConfig.java
│   └── SecurityConfig.java
├── filter/
│   ├── CorrelationIdFilter.java
│   └── JwtAuthenticationFilter.java
└── exception/
    └── GatewayExceptionHandler.java
```

### 2.5 Service Discovery (`service-discovery`)
```
com.chellaa.service_discovery/
└── ServiceDiscoveryApplication.java (with @EnableEurekaServer)
```

---

## 3. Review Checklist
- [x] Symmetrical, Clean Architecture package organization applied across all microservices.
- [x] Clear package distinctions for context, controllers, services, repositories, DTOs, entities, and outbox pollers.
- [x] Aligned with discovered initial package root `com.chellaa.*`.

---

## 4. Change History
| Version | Date | Author | Summary of Changes |
|---|---|---|---|
| 1.0.0 | 2026-11-03 | Software Architect | Initial baseline Package Structure document. |

