# Deployment Guide

Complete instructions for deploying MkSafeNet_Kids to production environments.

## Deployment Options

| Option | Complexity | Cost | Scaling | Setup Time |
|--------|-----------|------|---------|-----------|
| **Docker** | Medium | Low | Excellent | 30 min |
| **VPS + systemd** | Low | Low | Limited | 20 min |
| **Cloud (Heroku)** | Low | Medium | Good | 15 min |
| **Cloud (AWS/Azure)** | High | Medium-High | Excellent | 1-2 hours |

## Prerequisites

- Git repository (GitHub, GitLab, etc.)
- Domain name (for HTTPS)
- SSL/TLS certificate (Let's Encrypt free option)
- Production database (SQLite file or external DB)

## Option 1: Docker Deployment (Recommended)

### Step 1: Create Dockerfile for Backend

**File**: `backend/Dockerfile`

```dockerfile
FROM openjdk:17-slim

WORKDIR /app

# Build argument for environment
ARG JAR_FILE=target/mksafenet-*.jar

# Copy built JAR
COPY ${JAR_FILE} app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.loader.JarLauncher

# Run application with environment variables
ENTRYPOINT ["java", "-jar", "app.jar", \
  "--spring.datasource.url=${DATABASE_URL}", \
  "--jwt.secret=${JWT_SECRET}", \
  "--app.frontend.url=${FRONTEND_URL}"]
```

### Step 2: Create Dockerfile for Frontend

**File**: `frontend/Dockerfile`

```dockerfile
FROM node:18-alpine as builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine

COPY --from=builder /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

**File**: `frontend/nginx.conf`

```nginx
user nginx;
worker_processes auto;
error_log /var/log/nginx/error.log warn;
pid /var/run/nginx.pid;

events {
    worker_connections 1024;
}

http {
    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    access_log /var/log/nginx/access.log;

    sendfile on;
    keepalive_timeout 65;

    server {
        listen 80;
        server_name _;

        root /usr/share/nginx/html;
        index index.html;

        # Serve static files
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
        }

        # API proxy (optional if frontend and backend on different servers)
        location /api/ {
            proxy_pass http://backend:8080/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # SPA fallback
        location / {
            try_files $uri $uri/ /index.html;
        }
    }
}
```

### Step 3: Create docker-compose.yml

**File**: `docker-compose.yml`

```yaml
version: '3.8'

services:
  backend:
    build:
      context: ./backend
      dockerfile: Dockerfile
    container_name: mksafenet-backend
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:sqlite:/data/mksafenet.db
      JWT_SECRET: ${JWT_SECRET:-change-me-in-production}
      FRONTEND_URL: http://localhost:3000
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
    volumes:
      - db-data:/data
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    container_name: mksafenet-frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    environment:
      VITE_API_BASE_URL: http://localhost:8080
    restart: unless-stopped

volumes:
  db-data:
```

### Step 4: Build and Run Containers

```powershell
# Build backend JAR first
cd backend
mvn clean package -DskipTests

cd ..

# Build and start containers
docker-compose up --build

# Run in background
docker-compose up -d

# Check status
docker-compose ps

# View logs
docker-compose logs -f backend
docker-compose logs -f frontend

# Stop containers
docker-compose down
```

## Option 2: VPS Deployment with systemd (Linux)

### Step 1: Prepare VPS

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Java
sudo apt install -y openjdk-17-jre-headless

# Install Node.js (for frontend build)
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs

# Create app directory
sudo mkdir -p /opt/mksafenet
sudo chown $USER:$USER /opt/mksafenet
```

### Step 2: Build and Deploy Backend

```bash
# Clone repository
cd /tmp
git clone https://github.com/yourorg/mksafenet.git
cd mksafenet/backend

# Build JAR
mvn clean package -DskipTests -P production

# Copy to app directory
cp target/mksafenet-*.jar /opt/mksafenet/backend.jar

# Create systemd service
sudo tee /etc/systemd/system/mksafenet-backend.service > /dev/null <<EOF
[Unit]
Description=MkSafeNet Backend
After=network.target

[Service]
User=mksafenet
WorkingDirectory=/opt/mksafenet
ExecStart=/usr/bin/java -jar backend.jar \
  --spring.datasource.url=jdbc:sqlite:/opt/mksafenet/mksafenet.db \
  --jwt.secret=${JWT_SECRET} \
  --app.frontend.url=https://yourdomain.com \
  --server.port=8080
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Create mksafenet user
sudo useradd -r -s /bin/bash mksafenet 2>/dev/null || true
sudo chown mksafenet:mksafenet /opt/mksafenet

# Enable and start service
sudo systemctl enable mksafenet-backend
sudo systemctl start mksafenet-backend

# Check status
sudo systemctl status mksafenet-backend
sudo journalctl -u mksafenet-backend -f  # Follow logs
```

### Step 3: Deploy Frontend

```bash
# Build frontend
cd /tmp/mksafenet/frontend
npm install
npm run build

# Copy to web server directory
sudo mkdir -p /var/www/mksafenet
sudo cp -r dist/* /var/www/mksafenet/

# Install Nginx
sudo apt install -y nginx

# Create Nginx config
sudo tee /etc/nginx/sites-available/mksafenet > /dev/null <<EOF
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;

    root /var/www/mksafenet;
    index index.html;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    # SSL certificates (Let's Encrypt)
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    root /var/www/mksafenet;
    index index.html;

    # API proxy
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }

    # SPA fallback
    location / {
        try_files $uri $uri/ /index.html;
    }

    # Static assets caching
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/mksafenet /etc/nginx/sites-enabled/
sudo rm /etc/nginx/sites-enabled/default

# Test Nginx config
sudo nginx -t

# Start Nginx
sudo systemctl enable nginx
sudo systemctl start nginx

# Setup SSL with Let's Encrypt
sudo apt install -y certbot python3-certbot-nginx
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com
```

### Step 4: Environment Variables

Create `.env` file (not committed to git):

```bash
# /opt/mksafenet/.env
JWT_SECRET=your-super-secure-random-secret-key-min-32-chars
DATABASE_URL=jdbc:sqlite:/opt/mksafenet/mksafenet.db
APP_FRONTEND_URL=https://yourdomain.com
```

Load in systemd service:

```bash
EnvironmentFile=/opt/mksafenet/.env
```

## Option 3: Heroku Deployment

### Step 1: Install Heroku CLI

```powershell
# Download and install
# https://devcenter.heroku.com/articles/heroku-cli

# Login
heroku login
```

### Step 2: Create Heroku Apps

```bash
# Create backend app
heroku create mksafenet-backend

# Create frontend app
heroku create mksafenet-frontend

# Add buildpacks
heroku buildpacks:add heroku/java -a mksafenet-backend
heroku buildpacks:add heroku/nodejs -a mksafenet-frontend
```

### Step 3: Configure Environment Variables

```bash
# Backend environment variables
heroku config:set JWT_SECRET=your-secret-key -a mksafenet-backend
heroku config:set APP_FRONTEND_URL=https://mksafenet-frontend.herokuapp.com -a mksafenet-backend
```

### Step 4: Deploy

```bash
# Deploy backend
git push heroku main -a mksafenet-backend

# Deploy frontend
git push heroku main -a mksafenet-frontend

# View logs
heroku logs --tail -a mksafenet-backend
```

## Post-Deployment Checklist

- [ ] Backend running on production URL
- [ ] Frontend accessible and loads
- [ ] HTTPS enabled with valid certificate
- [ ] Database file backed up
- [ ] JWT secret is strong (not default)
- [ ] CORS configured for production domain
- [ ] Email notifications configured (if applicable)
- [ ] Monitoring and logging enabled
- [ ] Database backups scheduled
- [ ] Firewall configured (only ports 80/443 open)
- [ ] Rate limiting configured
- [ ] Load balancer/reverse proxy configured (if needed)

## Monitoring

### Health Checks

```bash
# Check backend health
curl https://api.yourdomain.com/api/actuator/health

# Check frontend
curl https://yourdomain.com
```

### Logs

**Docker:**
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

**Systemd:**
```bash
sudo journalctl -u mksafenet-backend -f
sudo journalctl -u nginx -f
```

### Metrics

Enable Spring Boot Actuator in `application.properties`:

```properties
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=when-authorized
```

Access metrics: `https://api.yourdomain.com/api/actuator/metrics`

## Scaling Strategies

### Database
- Use external PostgreSQL/MySQL for multiple backend instances
- Connection pooling (HikariCP)

### Backend
- Horizontal scaling with load balancer (Nginx, HAProxy)
- Container orchestration (Kubernetes)

### Frontend
- CDN for static assets (CloudFlare, AWS CloudFront)
- Browser caching headers

## Backup and Recovery

### Automated Backups

```bash
# Cron job (daily at 2 AM)
0 2 * * * tar -czf /backups/mksafenet-$(date +\%Y\%m\%d).tar.gz /opt/mksafenet/mksafenet.db

# Upload to cloud storage
0 3 * * * aws s3 cp /backups/mksafenet-*.tar.gz s3://my-backup-bucket/
```

### Restore from Backup

```bash
# Stop services
sudo systemctl stop mksafenet-backend

# Restore database
tar -xzf mksafenet-backup.tar.gz -C /opt/mksafenet/

# Start services
sudo systemctl start mksafenet-backend
```

## Troubleshooting

### Backend Not Starting

```bash
# Check logs
sudo journalctl -u mksafenet-backend -n 50

# Verify database connectivity
sqlite3 /opt/mksafenet/mksafenet.db ".tables"

# Check port availability
sudo netstat -tlnp | grep 8080
```

### Frontend Not Loading

```bash
# Check Nginx configuration
sudo nginx -t

# Check Nginx error log
sudo tail -f /var/log/nginx/error.log

# Verify app files exist
ls -la /var/www/mksafenet/
```

### CORS Issues

Update backend `application.properties`:

```properties
app.frontend.url=https://yourdomain.com
```

### High Memory Usage

```java
// In systemd service, set Java options
ExecStart=/usr/bin/java -Xmx512m -Xms256m -jar backend.jar ...
```

---

See [Architecture](architecture.md) for system design and [Security & Authentication](security.md) for hardening guidelines.

