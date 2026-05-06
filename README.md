# Digital Cyber Seva

Full-stack workspace for service request operations with customer and admin flows.

## Stack
- Backend: Java Spring Boot MVC
- Admin Panel: Angular
- Customer App: Ionic Angular + Capacitor
- Database: MySQL
- File storage: Cloudinary (replaceable with Firebase Storage later)

## Auth Rules
- Customer login: mobile + 4-digit Security PIN
- Admin login: mobile/email + password
- Auth: JWT token
- Roles: `CUSTOMER`, `ADMIN`
- OTP: **Not used anywhere**

## Folders
- `backend` - REST APIs, auth, roles, request workflows
- `admin-web` - admin management UI scaffold
- `customer-app` - customer mobile/web UI scaffold

## Feature Coverage in Backend APIs
- Customer service browsing
- Service request submission
- Document upload and delete
- Tracking ID generation
- Shop UPI QR upload + customer QR display API
- QR/UPI/Cash manual payment proof upload
- Admin service management
- Admin request processing
- Admin status updates
- Admin manual payment verification (`PAID` / `UNPAID`)
- Admin final document upload

## Backend API Toolkit
- API examples: `backend/docs/backend-api-examples.md`
- Postman collection: `backend/docs/postman/digital-cyber-seva.postman_collection.json`
- Postman environment: `backend/docs/postman/digital-cyber-seva-local.postman_environment.json`
- Account deletion Postman runbook: `backend/README.md` (`Postman Quick Test Flow (Account Deletion)` section)
- Validation/production setup guide: `VALIDATION_AND_PRODUCTION_GUIDE.md`

## Quick Start
1. Start MySQL and create database `digital_cyber_seva`.
2. Copy and configure environment values:
   - `backend/.env.example` -> `backend/.env` (or export env vars in shell/hosting platform)
3. Start backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Start admin panel:
   ```bash
   cd ../admin-web
   npm install
   npm start
   ```
5. Start customer app:
   ```bash
   cd ../customer-app
   npm install
   npm start
   ```

## Before Validation / Production
1. Never commit secrets in `application.properties`.
2. Set production values via environment variables:
   - DB: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
   - JWT: `JWT_SECRET`
   - Storage: `STORAGE_PROVIDER`, `CLOUDINARY_*` (if using Cloudinary)
   - CORS: `APP_CORS_ADDITIONAL_ORIGIN_PATTERNS`
3. Keep `STORAGE_PROVIDER=local` for low-cost staging if needed.
4. Ensure cron cleanup jobs are enabled (already configured by default):
   - account deletion cleanup
   - past records archive/cleanup
5. Build with production configs:
   - Admin: `cd admin-web && npm run build -- --configuration production`
   - Customer: `cd customer-app && npm run build -- --configuration production`
6. Validate full flow for 2-3 days on staging before production cutover.

## GitHub Collaboration Setup
1. Branch protection checklist:
   - `.github/BRANCH_PROTECTION_CHECKLIST.md`
2. Issue templates:
   - `.github/ISSUE_TEMPLATE/bug_report.yml`
   - `.github/ISSUE_TEMPLATE/feature_request.yml`
3. Pull request template:
   - `.github/pull_request_template.md`
4. CI workflow:
   - `.github/workflows/ci.yml`
   - Runs on `push` and `pull_request` to `main`
   - Checks:
     - `backend-build`
     - `admin-web-build`
     - `customer-app-build`
