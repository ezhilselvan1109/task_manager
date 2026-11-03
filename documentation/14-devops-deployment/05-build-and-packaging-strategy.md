# Build and Packaging Strategy

**Document Identifier:** DOC-OPS-005  
**Traceability References:** NFR-007, DOC-OPS-001, DOC-INF-004  
**Packaging Technologies:** Maven 3.9.6, Spring Boot Layered JAR, Vite / Rollup  
**Status:** Approved Baseline  

---

## 1. Executive Summary
This document specifies the artifact compilation, packaging optimization, and container layering strategies across the Task Management platform. By utilizing Spring Boot 3 layered JAR packaging and Vite asset bundling, build times and container deployment latencies are reduced by over 80%.

---

## 2. Spring Boot Layered JAR Architecture

Standard Spring Boot executable fat JARs contain application code bundled together with third-party dependencies. Rebuilding the Docker image invalidates the entire layer (~80 MB).

The platform mandates **Layered JAR packaging** via `spring-boot-maven-plugin`:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <configuration>
        <layers>
            <enabled>true</enabled>
        </layers>
    </configuration>
</plugin>
```

```mermaid
graph TD
    subgraph "Layered JAR Extraction (jarmode=layertools)"
        FatJar["Executable JAR (app.jar)"] --> L1["Layer 1: dependencies/ (~70 MB - Highly Cached)"]
        FatJar --> L2["Layer 2: spring-boot-loader/ (~200 KB)"]
        FatJar --> L3["Layer 3: snapshot-dependencies/ (~5 MB)"]
        FatJar --> L4["Layer 4: application/ (~500 KB - Code Changes Only)"]
    end
```

### 2.1 Optimized Multi-Stage Dockerfile Layer Copying
```dockerfile
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Copy layers separately to maximize Docker cache hits
COPY --from=builder /workspace/target/extracted/dependencies/ ./
COPY --from=builder /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=builder /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=builder /workspace/target/extracted/application/ ./

USER appuser
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "org.springframework.boot.loader.launch.JarLauncher"]
```
When a developer modifies a service class, only **Layer 4** is transferred to Docker, slashing container build and push times from 45 seconds to **under 2 seconds**.

---

## 3. Frontend Vite Production Packaging

Configured in `frontend/vite.config.ts`:
- **Code Splitting**: Vendor dependencies (`react`, `react-dom`, `axios`, `lucide-react`) are isolated into a dedicated `vendor` chunk.
- **Minification**: Built with esbuild for sub-second minification.
- **Tree-Shaking**: Discards unused Tailwind utilities and dead library code, capping total bundle payload at **< 300 KB gzip**.

