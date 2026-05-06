# Backend (Spring Boot)

## Stack
- Java 17+
- Spring Boot (Web, Data JPA, Security, Validation)
- JWT auth + BCrypt password hashing
- MySQL
- Cloudinary file storage
- Local file storage fallback (`/uploads/**`) when Cloudinary is unavailable
- Maven

## Architecture
- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `security`
- `config`
- `exception`

## Authentication Rules
- Customer login with mobile + 4-digit Security PIN
- Admin login with mobile/email + password
- No OTP anywhere
- Roles: `CUSTOMER`, `ADMIN`
- JWT returned after login

## Implemented APIs

### Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/set-pin` (migration for existing password-based customer accounts)

### Public/Customer
- `GET /api/services`
- `GET /api/services/{id}`
- `GET /api/categories`
- `POST /api/requests`
- `GET /api/requests/my`
- `GET /api/requests/{id}`
- `POST /api/requests/{requestId}/documents`
- `DELETE /api/requests/{requestId}/documents/{documentId}`
- `POST /api/requests/{id}/payment-proof`
- `POST /api/account-deletion/request`
- `GET /api/account-deletion/status`

### Admin
- `GET /api/admin/dashboard`
- `POST /api/admin/categories`
- `PUT /api/admin/categories/{id}`
- `POST /api/admin/services`
- `PUT /api/admin/services/{id}`
- `GET /api/admin/requests`
- `GET /api/admin/requests/{id}`
- `PUT /api/admin/requests/{id}/status`
- `POST /api/admin/requests/{id}/final-document`
- `PUT /api/admin/requests/{id}/payment/verify`
- `GET /api/admin/account-deletion`
- `PUT /api/admin/account-deletion/{id}/approve`
- `PUT /api/admin/account-deletion/{id}/reject`

## Key Behavior
- Customer can access only own requests/documents
- Admin can access all requests
- Customer cannot access `/api/admin/**`
- Customer uploads only to own requests
- Customer or admin can delete request documents using `DELETE /api/requests/{requestId}/documents/{documentId}`
- Document delete removes both DB record and cloud file
- Payment proof supports screenshot upload and/or UPI transaction ID
- QR payment is manual (no payment gateway)
- Frontend should show delete confirmation before calling document delete API

## File Upload Module
- Supported files: `jpg`, `jpeg`, `png`, `pdf`
- Storage abstraction: `StorageService`
  - `uploadFile(MultipartFile file, String folder)`
  - `deleteFile(String storagePublicId)`
  - `getFileUrl(String storagePublicId)`
- Cloudinary implementation: `CloudinaryStorageService`
- Local fallback implementation: `LocalStorageService`
- Firebase alternative scaffold: `FirebaseStorageService` (TODO placeholder, not active)

### Storage Fallback Behavior
- If `storage.provider=cloudinary` and Cloudinary keys are missing/invalid, uploads automatically fall back to local storage when `storage.fallback-to-local=true`.
- Local files are served from `http://localhost:8080/uploads/**`.
- To force local storage directly, set `storage.provider=local`.

## API Docs and Postman
- Endpoint examples: `docs/backend-api-examples.md`
- Postman collection: `docs/postman/digital-cyber-seva.postman_collection.json`
- Postman environment: `docs/postman/digital-cyber-seva-local.postman_environment.json`

## Run
```bash
mvn spring-boot:run
```

## Test
- `mvn test` uses H2 in-memory DB via `src/test/resources/application-test.properties`.
- MySQL is not required for running unit/context tests.

## Configuration
`src/main/resources/application.properties` is now environment-variable driven.

Use one of these approaches:
1. Export environment variables in terminal/host.
2. Keep a local-only `backend/.env` (copy from `backend/.env.example`) and load it before run.

Important variables:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `JWT_SECRET`, `JWT_EXPIRATION_MS`
- `STORAGE_PROVIDER` (`local` or `cloudinary`)
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET` (if cloudinary)
- `APP_CORS_ADDITIONAL_ORIGIN_PATTERNS`

Security note:
- Do not commit real secrets in any tracked file.
- If secrets were committed earlier, rotate/revoke them before deployment.

## Seed Data
- Default admin user:
  - Email: `admin@digitalcyberseva.com`
  - Mobile: `9999999999`
  - Password: `Admin@123`
- Customer registration requires `mobile + pin` (`pin` must be 4 digits, hashed with BCrypt).
- Sample categories and services are auto-seeded on first run.

## Postman Quick Test Flow (Account Deletion)
Use folder `Account Deletion` from:
`docs/postman/digital-cyber-seva.postman_collection.json`

Prerequisite:
1. Run `Auth > Login Customer`
2. Run `Auth > Login Admin`

Run order:
1. `Account Deletion > Request Account Deletion (Customer)`
2. `Account Deletion > Get My Deletion Status (Customer)`
3. `Account Deletion > List Account Deletion Requests (Admin)`
4. `Account Deletion > Approve Account Deletion Request (Admin)` or `Reject Account Deletion Request (Admin)`
5. Re-run `Account Deletion > Get My Deletion Status (Customer)`

What to expect:
1. Step 1 returns: `Your deletion request is submitted and pending admin approval.`
2. Step 2 should show `deletionStatus = PENDING`.
3. Step 3 auto-sets collection variable `deletionRequestId` from response.
4. Approve call sets customer status to `APPROVED` (soft delete only).
5. Reject call sets customer status to `REJECTED`.
6. Permanent delete runs later by scheduler after retention window (default 7 days).
