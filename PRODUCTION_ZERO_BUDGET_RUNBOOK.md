# Digital Cyber Seva - Zero Budget Production Runbook

This runbook helps you go live with minimum/zero budget using:
- Backend + MySQL on one free VM
- Admin web + Customer web/PWA on static hosting

---

## 1) Recommended Stack (Zero Budget)

1. Backend API (Spring Boot): Oracle Cloud Always Free VM
2. MySQL DB: same VM
3. Admin web (`admin-web`): Cloudflare Pages
4. Customer web/PWA (`customer-app`): Cloudflare Pages

---

## 2) Before Production (Local Final Check)

Run from project root:

```bash
cd "/Users/subhadeeppal/Documents/Digital Cyber Seva/digital-cyber-seva"
```

Build checks:

```bash
cd backend && mvn clean package -DskipTests
cd ../admin-web && npm ci && npm run build -- --configuration production
cd ../customer-app && npm ci && npm run build -- --configuration production
```

---

## 3) Set Production API URL in Frontends

Update:
- `admin-web/src/environments/environment.prod.ts`
- `customer-app/src/environments/environment.prod.ts`

Set:

```ts
apiBaseUrl: 'https://<YOUR_BACKEND_DOMAIN>/api'
```

Then commit and push:

```bash
git add .
git commit -m "chore: production api urls"
git push origin main
```

---

## 4) Backend + MySQL on Free VM

## 4.1 Create VM

- Create Oracle Cloud Always Free VM (Ubuntu)
- Open inbound ports: `22`, `80`, `443`

## 4.2 Install runtime

```bash
sudo apt update && sudo apt -y upgrade
sudo apt install -y openjdk-17-jre-headless maven mysql-server caddy git
```

## 4.3 Prepare database

```sql
CREATE DATABASE IF NOT EXISTS digital_cyber_seva CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'dcs_user'@'localhost' IDENTIFIED BY 'CHANGE_ME_STRONG_PASSWORD';
GRANT ALL PRIVILEGES ON digital_cyber_seva.* TO 'dcs_user'@'localhost';
FLUSH PRIVILEGES;
```

## 4.4 Deploy backend code

```bash
cd /opt
sudo git clone https://github.com/Subhadeep075/MADMAX.git dcs
sudo chown -R $USER:$USER /opt/dcs
cd /opt/dcs/backend
mvn clean package -DskipTests
```

## 4.5 Configure backend env

Create env file:

```bash
mkdir -p /opt/dcs/backend/runtime
cat > /opt/dcs/backend/runtime/.env <<'EOF'
SERVER_PORT=8080
DB_URL=jdbc:mysql://127.0.0.1:3306/digital_cyber_seva?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Kolkata
DB_USERNAME=dcs_user
DB_PASSWORD=CHANGE_ME_STRONG_PASSWORD
JPA_DDL_AUTO=update
JPA_SHOW_SQL=false
JPA_FORMAT_SQL=false
JWT_SECRET=REPLACE_WITH_LONG_RANDOM_SECRET_AT_LEAST_32_CHARS
JWT_EXPIRATION_MS=86400000

# Add your actual admin/customer frontend domains here
APP_CORS_ADDITIONAL_ORIGIN_PATTERNS=https://<ADMIN_PAGES_DOMAIN>,https://<CUSTOMER_PAGES_DOMAIN>

# Storage options: local (zero cost) or cloudinary
STORAGE_PROVIDER=local
STORAGE_FALLBACK_TO_LOCAL=true
LOCAL_STORAGE_BASE_PATH=/opt/dcs/storage/uploads
LOCAL_STORAGE_PUBLIC_BASE_URL=https://<YOUR_BACKEND_DOMAIN>/uploads

# Optional if using cloudinary
CLOUDINARY_CLOUD_NAME=
CLOUDINARY_API_KEY=
CLOUDINARY_API_SECRET=
EOF
```

## 4.6 systemd service

Create service file:

```bash
sudo tee /etc/systemd/system/dcs-backend.service >/dev/null <<'EOF'
[Unit]
Description=Digital Cyber Seva Backend
After=network.target mysql.service

[Service]
User=ubuntu
WorkingDirectory=/opt/dcs/backend
EnvironmentFile=/opt/dcs/backend/runtime/.env
ExecStart=/usr/bin/java -jar /opt/dcs/backend/target/backend-0.0.1-SNAPSHOT.jar
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF
```

Start service:

```bash
sudo systemctl daemon-reload
sudo systemctl enable dcs-backend
sudo systemctl restart dcs-backend
sudo systemctl status dcs-backend --no-pager
```

Logs:

```bash
journalctl -u dcs-backend -f
```

## 4.7 HTTPS reverse proxy (Caddy)

Set `/etc/caddy/Caddyfile`:

```caddy
<YOUR_BACKEND_DOMAIN> {
    reverse_proxy 127.0.0.1:8080
}
```

Reload:

```bash
sudo systemctl reload caddy
sudo systemctl status caddy --no-pager
```

---

## 5) Deploy Admin + Customer on Cloudflare Pages

Create two separate Pages projects from same repo.

## Project A: Admin
- Root directory: `admin-web`
- Build command: `npm ci && npm run build -- --configuration production`
- Build output directory: `dist/digital-cyber-seva-admin-web/browser`

## Project B: Customer
- Root directory: `customer-app`
- Build command: `npm ci && npm run build -- --configuration production`
- Build output directory: `dist/customer-app/browser`

After deploy, note both `*.pages.dev` URLs and put them in backend env:
- `APP_CORS_ADDITIONAL_ORIGIN_PATTERNS`

Then restart backend:

```bash
sudo systemctl restart dcs-backend
```

---

## 6) Go-Live Verification

1. Admin login works from Pages URL
2. Customer login/register works from Pages URL
3. Customer request flow works (create request, upload docs, payment proof)
4. Admin verification flow works (payment verify, status update, final doc)
5. CORS errors are gone
6. Uploaded files open correctly via backend uploads URL

---

## 7) Daily Ops (Free Tier Friendly)

1. DB backup once daily
2. Check logs once daily
3. Keep `STORAGE_PROVIDER=local` until you need CDN storage
4. Keep only required CORS origins (no wildcard in production)

---

## 8) If You Change Code Later

After local changes:

```bash
git add .
git commit -m "feat/fix: your change"
git push origin main
```

Then:
- Cloudflare Pages auto redeploys frontend.
- On VM:
  - `cd /opt/dcs && git pull`
  - `cd backend && mvn clean package -DskipTests`
  - `sudo systemctl restart dcs-backend`

