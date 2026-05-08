# Digital Cyber Seva

Digital Cyber Seva is a full-stack platform for service requests, document handling, and manual payment verification.
It has two separate apps:
- Customer app (Ionic Angular) for citizens
- Admin panel (Angular) for operators

## Tech Stack
- Backend: Spring Boot (Java 17)
- Database: MySQL
- Admin UI: Angular
- Customer UI: Ionic Angular + Capacitor (APK + Web/PWA)
- Auth: JWT

## Authentication Model
- Customer: mobile number + 4-digit Security PIN
- Admin: mobile/email + password
- Roles: `CUSTOMER`, `ADMIN`
- OTP is intentionally not used

## Project Structure
- `backend` - APIs, business logic, security, scheduled jobs
- `admin-web` - admin dashboard and operations
- `customer-app` - customer mobile/web experience

## Core Functional Flows
- Browse categories and services
- Create customer request with tracking ID
- Upload and delete request documents
- Submit payment proof (UPI ID or screenshot)
- Admin verifies payment manually
- Admin updates request status and uploads final document
- Customer sees status updates and final document
- Account deletion request with admin approval and delayed cleanup

## Local Setup
1. Create MySQL database: `digital_cyber_seva`
2. Copy env template:
   - `backend/.env.example` -> `backend/.env`
3. Start backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
4. Start admin app:
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

## API and Testing References
- API examples: `backend/docs/backend-api-examples.md`
- Postman collection: `backend/docs/postman/digital-cyber-seva.postman_collection.json`
- Postman environment: `backend/docs/postman/digital-cyber-seva-local.postman_environment.json`
- Account deletion API flow: `backend/README.md`

## Deployment and Validation Docs
- Validation guide: `VALIDATION_AND_PRODUCTION_GUIDE.md`
- Production deployment runbook: `PRODUCTION_DEPLOYMENT_RUNBOOK.md`

## Team Workflow
- Branch protection checklist: `.github/BRANCH_PROTECTION_CHECKLIST.md`
- Issue templates: `.github/ISSUE_TEMPLATE/`
- PR template: `.github/pull_request_template.md`
- CI workflow: `.github/workflows/ci.yml`
