# =========================================================================
# Stage 1: Build Layer Extractor using Multi-Module Maven Parent
# =========================================================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy parent and all module POM files to maximize Docker layer caching
COPY pom.xml .
COPY common/pom.xml common/
COPY service-discovery/pom.xml service-discovery/
COPY api-gateway/pom.xml api-gateway/
COPY user-service/pom.xml user-service/
COPY task-service/pom.xml task-service/
COPY notification-service/pom.xml notification-service/

# Cache Maven dependencies offline for service-discovery
RUN mvn dependency:go-offline -B -pl service-discovery -am || true

# Copy service-discovery source code
COPY service-discovery/src service-discovery/src

# Package service-discovery jar
RUN mvn clean package -pl service-discovery -am -DskipTests -B

# Extract Spring Boot layered JAR for optimal Docker caching
WORKDIR /workspace/service-discovery/target
RUN java -Djarmode=layertools -jar service-discovery-*.jar extract

# =========================================================================
# Stage 2: Hardened, Unprivileged Runtime Container
# =========================================================================
FROM eclipse-temurin:21-jre-alpine AS runner
WORKDIR /app

# Security: Create non-root unprivileged user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Install curl for Actuator health check probes
RUN apk add --no-cache curl

# Copy extracted layers from builder stage with strict non-root ownership
COPY --from=builder --chown=appuser:appgroup /workspace/service-discovery/target/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/service-discovery/target/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/service-discovery/target/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /workspace/service-discovery/target/application/ ./

# Enforce non-root execution
USER appuser:appgroup

# Configurable JVM memory settings adhering to Hostinger VPS budget
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:+ExitOnOutOfMemoryError"
ENV SERVER_PORT=8761
EXPOSE 8761

HEALTHCHECK --interval=10s --timeout=5s --retries=5 --start-period=30s \
  CMD curl -f http://localhost:8761/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
