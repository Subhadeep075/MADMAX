# Validation and Production Guide (Low/No Budget)

This guide helps you validate safely for 2-3 days and then move to production with minimum cost.

## 1) What is already cleaned up

- Backend secrets moved to environment variables.
- `backend/.env.example` added as template.
- `.gitignore` updated for env files, uploads/archives, Angular/Ionic build outputs, Android build artifacts.
- Frontend production environment files are in place:
  - `admin-web/src/environments/environment.prod.ts`
  - `customer-app/src/environments/environment.prod.ts`

## 2) What you should still do before public validation

1. Create real local env file:
   - Copy `backend/.env.example` to `backend/.env`.
   - Fill real DB/JWT/CORS/storage values.
2. Rotate any old secrets that were previously pasted in files/chats.
3. Build-check all apps:
   - `cd backend && mvn -DskipTests compile`
   - `cd ../admin-web && CI=1 npx ng build --configuration production --progress=false`
   - `cd ../customer-app && CI=1 npx ng build --configuration production --progress=false`
4. Keep file storage local for validation:
   - `STORAGE_PROVIDER=local`
5. Run 2-3 day validation with real flows:
   - customer registration/login
   - request + document upload/delete
   - payment proof upload
   - admin verify + status update + final doc

## 3) Recommended low/no-cost deployment shape

## Option A (best for strict zero budget)

- Backend + DB on one always-free VM.
- Admin web + Customer PWA as static frontend hosting.

Suggested shape:

- VM: Oracle Cloud Always Free compute.
- DB: MySQL on same VM (or OCI Always Free MySQL HeatWave if available in your region/account).
- Frontend hosting: Cloudflare Pages (static).

## Option B (easy staging only)

- Render free web service for backend (not for production-grade reliability).
- Free static hosting for frontends.

## 4) Production env checklist

Set these in production backend environment:

- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `APP_CORS_ADDITIONAL_ORIGIN_PATTERNS`
- `STORAGE_PROVIDER`
- Cloudinary vars only if using Cloudinary:
  - `CLOUDINARY_CLOUD_NAME`
  - `CLOUDINARY_API_KEY`
  - `CLOUDINARY_API_SECRET`

For frontend production builds:

- Set real backend API domain in:
  - `admin-web/src/environments/environment.prod.ts`
  - `customer-app/src/environments/environment.prod.ts`

## 5) Public go-live safety

Before go-live:

1. Disable debug logs.
2. Confirm CORS allows only your real domains.
3. Verify account deletion + cleanup jobs run on schedule.
4. Take DB backup snapshot.
5. Test login and one full request flow from phone over mobile network (not only localhost Wi-Fi).

After go-live:

- Monitor logs daily for first week.
- Keep 30-day archive export working.
- Review storage size every 2-3 days.

