# Admin Web (Angular)

## Scope
- Admin login using mobile/email + password (no OTP)
- JWT interceptor + auth/role route guards
- Admin-only routed layout with left sidebar
- Dashboard cards from `/api/admin/dashboard`
- Category management (add/edit/activate/deactivate/display order)
- Service management (add/edit/status/dates/fees/required documents/search/filter)
- Request queue with filters (status/payment/service/date)
- Request detail (document preview/delete with confirmation, status update, manual payment verification, final document upload)
- Payment settings (upload/update fixed UPI QR and view current setting)

## Run
```bash
npm install
npm start
```

## Notes
- API base URL is configured in `src/environments/environment.ts`.
- If Angular `ng build` crashes on very new Node versions, use Node LTS (20/22).
- Angular compiler verification can be run with:
  ```bash
  npx ngc -p tsconfig.app.json
  ```
