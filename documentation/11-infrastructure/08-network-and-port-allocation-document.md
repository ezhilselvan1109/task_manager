# Network and Port Allocation Document

**Document Identifier:** DOC-INF-008  
**Traceability References:** NFR-007, DOC-INF-001, DOC-INF-005  
**Network Scope:** Docker Bridge Subnet `172.28.0.0/16`  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document defines the network architecture, container DNS naming conventions, and port allocation schema across the Task Management platform. It explicitly delineates public edge ports from private, internal container network interfaces.

---

## 2. Port Allocation Matrix

| Service | Internal Container Port | Host Exposed Port | Access Scope | Protocol | Description |
|---|---|---|---|---|---|
| `frontend` | `80` | `127.0.0.1:3000` | **Public** | HTTP | React SPA web user interface. |
| `api-gateway` | `8080` | `127.0.0.1:8080` | **Public** | HTTP | Central edge API ingress point. |
| `grafana` | `3000` | `127.0.0.1:3001` | **Admin** | HTTP | Observability metrics dashboard (remapped to avoid 3000 conflict). |
| `prometheus` | `9090` | `127.0.0.1:9090` | **Admin** | HTTP | Prometheus metrics query engine. |
| `service-discovery`| `8761` | `127.0.0.1:8761` | **Internal** | HTTP | Eureka registry UI (bound to localhost for dev). |
| `postgresql` | `5432` | `127.0.0.1:5432` | **Internal** | PostgreSQL | Multi-tenant database server. |
| `kafka` (internal)| `9092` | Not exposed | **Internal** | Kafka TCP | Inter-container Kafka messaging. |
| `kafka` (external)| `9094` | `127.0.0.1:9094` | **Internal** | Kafka TCP | Host listener for local IDE hybrid development. |
| `zookeeper` | `2181` | Not exposed | **Internal** | Zookeeper | Kafka coordination daemon. |
| `user-service` | `8081` | Not exposed | **Internal** | HTTP | Core user management service. |
| `task-service` | `8082` | Not exposed | **Internal** | HTTP | Core task management service. |
| `notification` | `8083` | Not exposed | **Internal** | HTTP | Core notification service. |

---

## 3. Network Architecture Specifications

1. **Docker Network Definition**:
   ```yaml
   networks:
     taskmanager-network:
       name: taskmanager-network
       driver: bridge
       ipam:
         config:
           - subnet: 172.28.0.0/16
             gateway: 172.28.0.1
   ```
2. **Service Discovery via Embedded DNS**:
   - Docker daemon resolves service names (e.g. `http://service-discovery:8761`, `jdbc:postgresql://postgresql:5432/...`) automatically using container names on `taskmanager-network`.
3. **Loopback Host Binding (`127.0.0.1`)**:
   - All host-published ports bind strictly to `127.0.0.1` rather than `0.0.0.0`, preventing exposed developer ports from being accessed over local LAN/Wi-Fi networks.

