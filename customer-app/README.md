# Customer App (Ionic Angular + Capacitor)

## Stack
- Ionic Angular (standalone components)
- Angular 18
- Capacitor
- JWT auth (no OTP)

## Implemented Features
- Login and Register (mobile + 4-digit Security PIN, no OTP)
- Bottom tabs: Home, Services, My Requests, Profile
- Home page: visible search bar, categories, popular open services
- Service list: search + category filter + status badge (OPEN/CLOSED/COMING_SOON)
- Service detail: fee breakup, required documents, apply button
- Request form: applicant details + dynamic placeholder fields + document upload preview/delete
- Review & Submit: confirms all data before creating request
- Payment page: fee details, UPI QR display, screenshot/UPI ID proof, pay-at-shop option
- My requests list + request detail timeline
- Request detail includes document delete confirmation and final document download
- Profile page supports customer name edit (save/cancel, validation, toast)
- Account deletion request flow with status (NONE/PENDING/APPROVED/REJECTED)
- Browser/PWA-ready customer app (same codebase as APK)

## Core App Infrastructure
- API service layer
- Auth service + JWT storage
- Route guard + HTTP interceptor
- TypeScript API interfaces
- Request draft state service
- Loading indicators + toast messages + simple error text

## Run
```bash
npm install
npm start
```

App URL:
- `http://localhost:4200`

## Build
```bash
npm run build
npm run build:prod
npm run web:build
```

## Capacitor
```bash
npx cap sync
npm run android
npm run android:build
npm run ios
```

## API Base URL
Update as needed:
- Local/dev: `src/environments/environment.ts`
- Production/web: `src/environments/environment.prod.ts`

Default:
- `http://localhost:8080/api`

## PWA + Browser Access
- Manifest: `src/manifest.webmanifest`
- Service worker: `src/sw.js`
- Router uses hash mode for SPA hosting compatibility.
- For hosting (Firebase/Netlify/Vercel), route all paths to `index.html`.

## Deploy Notes (Customer App Only)
- This app contains only customer routes/features.
- Admin panel remains separate in `admin-web`.
- For production CORS, set backend property:
  - `app.cors.additional-origin-patterns=https://your-customer-domain.com,https://your-admin-domain.com`
