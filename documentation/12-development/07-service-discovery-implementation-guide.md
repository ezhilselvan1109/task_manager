# Service Discovery Implementation Guide

**Document Identifier:** DOC-DEV-007  
**Traceability References:** ADR-003, DOC-INF-011  
**Target Module:** `service-discovery`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the implementation guide for `service-discovery`. Built with Spring Cloud Netflix Eureka Server, the service acts as the dynamic service registry enabling Spring Cloud Gateway and microservices to resolve instance network locations dynamically without hardcoded hostnames or IP addresses.

---

## 2. Dependencies (`service-discovery/pom.xml`)

```xml
<dependencies>
    <!-- Eureka Server Core -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
    </dependency>

    <!-- Health Probing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

---

## 3. Application Class & Annotations

```java
package com.taskmanager.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class ServiceDiscoveryApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceDiscoveryApplication.class, args);
    }
}
```

---

## 4. Configuration Specification (`application.yml`)

```yaml
server:
  port: 8761

spring:
  application:
    name: service-discovery

eureka:
  instance:
    hostname: service-discovery
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: always
```

---

## 5. Client Microservice Registration Checklist
To register a microservice with Eureka:
1. Include `spring-cloud-starter-netflix-eureka-client` in the service's `pom.xml`.
2. Configure `spring.application.name` in `application.yml` (e.g. `task-service`).
3. Set `eureka.client.serviceUrl.defaultZone=http://service-discovery:8761/eureka/`.
4. Spring Boot 3 auto-configures registration upon application startup.

