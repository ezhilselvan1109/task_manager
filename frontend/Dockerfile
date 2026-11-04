# =========================================================================
# Stage 1: Build React SPA using Node.js 20 Alpine
# =========================================================================
FROM node:20-alpine AS builder
WORKDIR /app

# Copy package descriptors for deterministic dependency caching
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline --no-audit

# Copy application source code
COPY . .

# Injected Vite build arguments (passed from docker-compose / .env)
# Default is empty string so apiClient uses relative same-origin /api/... via Nginx reverse proxy
ARG VITE_API_GATEWAY_URL=""
ENV VITE_API_GATEWAY_URL=${VITE_API_GATEWAY_URL}

# Compile TypeScript and build production static bundle into /app/dist
RUN npm run build

# =========================================================================
# Stage 2: Serve Production Assets using Hardened Nginx Alpine
# =========================================================================
FROM nginx:1.25-alpine AS runner

# Remove default Nginx HTML landing page
RUN rm -rf /usr/share/nginx/html/*

# Copy custom Nginx configuration supporting SPA client-side routing & caching
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled static assets from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Hardening: Configure directories and ownership for non-root nginx user
RUN touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid /var/cache/nginx /var/log/nginx /usr/share/nginx/html /etc/nginx/conf.d

# Switch to unprivileged nginx user
USER nginx

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=3s --retries=3 --start-period=5s \
  CMD wget -q -O - http://localhost:80/healthz || exit 1

CMD ["nginx", "-g", "daemon off;"]

