# Setup - Production Deployment

Quick reference guide for deploying MkSafeNet_Kids to production environments.

## Overview

For comprehensive deployment instructions, see [Deployment Guide](deployment.md).

This document is a quick reference for common production setups.

## Environment Configuration

### Backend (application.properties for Production)

**File**: `backend/src/main/resources/application-prod.properties`

```properties
# Database
spring.datasource.url=jdbc:sqlite:/opt/mksafenet/mksafenet.db
# or for PostgreSQL:
# spring.datasource.url=jdbc:postgresql://db-host:5432/mksafenet
# spring.datasource.username=${DB_USER}
# spring.datasource.password=${DB_PASSWORD}
# spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# Security
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Frontend URL (for CORS)
app.frontend.url=https://yourdomain.com

# Server
server.port=8080
server.servlet.context-path=/api

# Logging
logging.level.root=WARN
logging.level.com.mksafenet=INFO

# Performance
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```

**Run with profile**:
```bash
java -jar backend.jar --spring.profiles.active=prod
```

### Frontend (.env.production)

**File**: `frontend/.env.production`

```env
VITE_API_BASE_URL=https://api.yourdomain.com
VITE_APP_TITLE=MkSafeNet Kids
```

## Quick Deployment Checklist

- [ ] Change `jwt.secret` to strong random value (≥32 characters)
- [ ] Set `app.frontend.url` to production domain
- [ ] Enable HTTPS with valid SSL certificate
- [ ] Configure database connection (SQLite or external)
- [ ] Set up automated backups for database
- [ ] Configure monitoring and logging
- [ ] Test all endpoints in production environment
- [ ] Set up rate limiting (optional but recommended)
- [ ] Enable CORS only for your domain
- [ ] Review security settings

## Docker Deployment (Simplest)

```bash
# Build and run
docker-compose up --build -d

# Stop
docker-compose down

# View logs
docker-compose logs -f backend
```

See [Deployment Guide](deployment.md) for Docker configuration.

## Linux VPS Deployment (Systemd)

### Backend Service

**File**: `/etc/systemd/system/mksafenet-backend.service`

```ini
[Unit]
Description=MkSafeNet Backend
After=network.target

[Service]
Type=simple
User=mksafenet
WorkingDirectory=/opt/mksafenet
EnvironmentFile=/opt/mksafenet/.env
ExecStart=/usr/bin/java -Xmx512m -jar backend.jar \
  --spring.profiles.active=prod
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

### Environment Variables

**File**: `/opt/mksafenet/.env`

```bash
JWT_SECRET=your-super-secure-secret-key-min-32-chars
APP_FRONTEND_URL=https://yourdomain.com
DATABASE_URL=jdbc:sqlite:/opt/mksafenet/mksafenet.db
```

### Enable and Start

```bash
sudo systemctl enable mksafenet-backend
sudo systemctl start mksafenet-backend
sudo systemctl status mksafenet-backend
```

## Nginx Reverse Proxy

**File**: `/etc/nginx/sites-available/mksafenet`

```nginx
server {
    listen 80;
    server_name yourdomain.com www.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    root /var/www/mksafenet;

    # Frontend SPA
    location / {
        try_files $uri /index.html;
    }

    # API proxy
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto https;
    }

    # Static assets caching
    location ~* \.(js|css|png|jpg|jpeg|gif|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

## SSL/TLS Certificate

### Let's Encrypt (Free)

```bash
# Install Certbot
sudo apt install certbot python3-certbot-nginx

# Obtain certificate
sudo certbot --nginx -d yourdomain.com -d www.yourdomain.com

# Auto-renew (runs daily)
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer
```

## Database Backups

### Automated Daily Backup

**File**: `/etc/cron.d/mksafenet-backup`

```bash
0 2 * * * mksafenet tar -czf /backups/mksafenet-$(date +\%Y\%m\%d).tar.gz /opt/mksafenet/mksafenet.db
```

### Cloud Upload (AWS S3)

```bash
0 3 * * * mksafenet aws s3 cp /backups/mksafenet-*.tar.gz s3://my-backup-bucket/
```

## Monitoring

### Health Check Endpoint

```bash
curl https://yourdomain.com/api/actuator/health
```

Enable in `application-prod.properties`:

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=when-authorized
```

### Log Monitoring

```bash
# Tail backend logs
sudo journalctl -u mksafenet-backend -f

# Tail Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

## Performance Tuning

### Java Heap Size

```bash
# In systemd service
ExecStart=/usr/bin/java -Xmx1g -Xms512m -jar backend.jar
```

### Database Connection Pool

**In `application-prod.properties`**:

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
```

### Nginx Caching

```nginx
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=api_cache:10m;

location /api/ {
    proxy_cache api_cache;
    proxy_cache_valid 200 10m;
    proxy_pass http://localhost:8080/api/;
}
```

## Troubleshooting Production

### Backend not starting

```bash
sudo systemctl status mksafenet-backend
sudo journalctl -u mksafenet-backend -n 100
```

### High CPU/Memory usage

```bash
# Check Java process
top -p $(pgrep -f backend.jar)

# Increase heap size in systemd service
sudo systemctl edit mksafenet-backend
```

### Database locked error

```bash
# Ensure only one backend process
ps aux | grep java

# Restart service
sudo systemctl restart mksafenet-backend
```

### HTTPS not working

```bash
# Check certificate validity
sudo certbot certificates

# Test SSL
openssl s_client -connect yourdomain.com:443

# Renew if expired
sudo certbot renew --force-renewal
```

## Security Best Practices

1. **Change default credentials**
2. **Use strong JWT secret** (≥32 random characters)
3. **Enable HTTPS only** (HTTP → HTTPS redirect)
4. **Regular backups** (daily, off-site)
5. **Monitor logs** for suspicious activity
6. **Rate limiting** on API endpoints
7. **Keep dependencies updated**
8. **Regular security scans** (OWASP, CVE checks)

---

See [Deployment Guide](deployment.md) for complete deployment documentation including Docker, AWS, Heroku, and other cloud platforms.

