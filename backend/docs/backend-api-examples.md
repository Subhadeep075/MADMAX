# Digital Cyber Seva Backend API Examples

Base URL: `http://localhost:8080`

## Auth Headers
- Customer APIs: `Authorization: Bearer <customer_jwt_token>`
- Admin APIs: `Authorization: Bearer <admin_jwt_token>`

## File Upload Rules
- Supported file extensions: `jpg`, `jpeg`, `png`, `pdf`
- Supported content types: `image/jpg`, `image/jpeg`, `image/png`, `application/pdf`
- File is stored in cloud storage and both `fileUrl` and `storagePublicId` are saved in DB.
- Delete operation removes cloud file first, then database row.
- Frontend should always show user confirmation dialog before delete API call.
- Do not log file contents in frontend/backend logs.

---

## 1. Register Customer
`POST /api/auth/register`

Request:
```json
{
  "name": "Ravi Kumar",
  "mobile": "9876543210",
  "email": "",
  "pin": "1234",
  "address": "Patna"
}
```

Response:
```json
{
  "userId": 2,
  "name": "Ravi Kumar",
  "role": "CUSTOMER",
  "token": "<jwt>"
}
```

## 2. Login Customer (Mobile + PIN)
`POST /api/auth/login`

Request:
```json
{
  "mobile": "9876543210",
  "pin": "1234"
}
```

Response:
```json
{
  "userId": 2,
  "name": "Ravi Kumar",
  "role": "CUSTOMER",
  "token": "<jwt>"
}
```

## 3. Login Admin (Mobile/Email + Password)
`POST /api/auth/login`

Request:
```json
{
  "username": "admin@digitalcyberseva.com",
  "password": "Admin@123"
}
```

## 4. Set PIN for Existing Customer (Migration API)
`POST /api/auth/set-pin`

Request:
```json
{
  "mobile": "9876543210",
  "oldPassword": "OldCustomerPassword",
  "pin": "1234"
}
```

Response:
```json
{
  "message": "PIN set successfully. Please login with mobile and PIN"
}
```

## 4.1 Get Customer Profile (Customer)
`GET /api/customer/profile`

Response:
```json
{
  "id": 2,
  "name": "Ravi Kumar",
  "mobile": "9876543210",
  "email": null,
  "address": "Patna"
}
```

## 4.2 Update Customer Name (Customer)
`PUT /api/customer/profile/name`

Request:
```json
{
  "name": "Ravi Kumar Updated"
}
```

Rules:
- Name is required
- Min 2 chars, max 80 chars
- Only logged-in customer can update own name

Response:
```json
{
  "id": 2,
  "name": "Ravi Kumar Updated",
  "mobile": "9876543210",
  "email": null,
  "address": "Patna"
}
```

---

## 5. List Services (Public)
`GET /api/services`

Response:
```json
[
  {
    "id": 1,
    "categoryId": 1,
    "categoryName": "Documents",
    "title": "PAN Card Apply",
    "description": "New PAN card application with document verification.",
    "requiredDocumentsJson": "[\"Aadhaar Card\",\"Photo\",\"Mobile Number\"]",
    "govtFee": 107.00,
    "serviceFee": 60.00,
    "totalFee": 167.00,
    "status": "OPEN",
    "openDate": "2026-04-26",
    "closeDate": null
  }
]
```

## 6. Get Service Details (Public)
`GET /api/services/{id}`

Response:
```json
{
  "id": 1,
  "categoryId": 1,
  "categoryName": "Documents",
  "title": "PAN Card Apply",
  "description": "New PAN card application with document verification.",
  "requiredDocumentsJson": "[\"Aadhaar Card\",\"Photo\",\"Mobile Number\"]",
  "govtFee": 107.00,
  "serviceFee": 60.00,
  "totalFee": 167.00,
  "status": "OPEN",
  "openDate": "2026-04-26",
  "closeDate": null
}
```

## 7. List Categories (Public)
`GET /api/categories`

Response:
```json
[
  {
    "id": 1,
    "name": "Documents",
    "iconName": "document-text-outline",
    "displayOrder": 1,
    "active": true
  }
]
```

---

## 8. Create Service Request (Customer)
`POST /api/requests`

Request:
```json
{
  "serviceId": 1,
  "remarks": "Please process quickly"
}
```

Response:
```json
{
  "id": 10,
  "trackingId": "DCS-20260428-AB12CD34",
  "customerId": 2,
  "customerName": "Ravi Kumar",
  "service": {
    "id": 1,
    "categoryId": 1,
    "categoryName": "Documents",
    "title": "PAN Card Apply",
    "description": "New PAN card application with document verification.",
    "requiredDocumentsJson": "[\"Aadhaar Card\",\"Photo\",\"Mobile Number\"]",
    "govtFee": 107.00,
    "serviceFee": 60.00,
    "totalFee": 167.00,
    "status": "OPEN",
    "openDate": "2026-04-26",
    "closeDate": null
  },
  "status": "PENDING",
  "totalAmount": 167.00,
  "paymentStatus": "UNPAID",
  "remarks": "Please process quickly",
  "createdAt": "2026-04-28T14:52:00",
  "updatedAt": "2026-04-28T14:52:00",
  "documents": [],
  "latestPayment": null
}
```

## 9. List My Requests (Customer)
`GET /api/requests/my`

Response: Array of `ApplicationRequestResponse`.

## 10. Get My Request by ID (Customer)
`GET /api/requests/{id}`

Response: `ApplicationRequestResponse`.

## 11. Upload Request Document (Customer)
`POST /api/requests/{requestId}/documents`

Form-data:
- `documentType` (text): `AADHAAR`
- `file` (file): `aadhaar.pdf`

Response:
```json
{
  "id": 55,
  "documentType": "AADHAAR",
  "fileName": "aadhaar.pdf",
  "fileUrl": "https://res.cloudinary.com/.../aadhaar.pdf",
  "uploadedAt": "2026-04-28T15:02:45"
}
```

## 12. Delete Request Document (Customer or Admin)
`DELETE /api/requests/{requestId}/documents/{documentId}`

Response:
```json
{
  "message": "Document deleted successfully"
}
```

Notes:
- Checks permission.
- Finds document by `requestId + documentId`.
- Deletes cloud file using `storagePublicId`.
- Deletes DB row.
- Creates audit log entry without sensitive file content.

## 13. Get Payment Settings (Public/Customer)
`GET /api/payment-settings`

Response:
```json
{
  "upiQrImageUrl": "https://res.cloudinary.com/.../digital-cyber-seva/payment-settings/upi-qr/sample.png",
  "shopUpiName": "Digital Cyber Seva Main",
  "shopUpiId": "shop@upi"
}
```

Notes:
- This is a fixed QR image uploaded by admin/shop owner.
- No dynamic QR generation and no automatic payment verification.

## 14. Submit Payment Proof (Customer)
`POST /api/requests/{id}/payment-proof`

Form-data:
- `method` (text): `UPI_QR`
- `upiTransactionId` (text, optional): `UPI123456789`
- `screenshot` (file, optional): `payment.png`

Response:
```json
{
  "id": 99,
  "amount": 167.00,
  "method": "UPI_QR",
  "upiTransactionId": "UPI123456789",
  "paymentProofUrl": "https://res.cloudinary.com/.../payment.png",
  "status": "PROOF_SUBMITTED",
  "createdAt": "2026-04-28T15:08:10"
}
```

---

## 15. Admin Dashboard
`GET /api/admin/dashboard`

Response:
```json
{
  "totalRequests": 120,
  "pendingRequests": 18,
  "inProgressRequests": 47,
  "completedRequests": 39,
  "proofSubmittedPayments": 16
}
```

## 16. List Categories (Admin - includes active and inactive)
`GET /api/admin/categories`

Response:
```json
[
  {
    "id": 1,
    "name": "Documents",
    "iconName": "document-text-outline",
    "displayOrder": 1,
    "active": true
  },
  {
    "id": 9,
    "name": "Old Category",
    "iconName": "folder-outline",
    "displayOrder": 99,
    "active": false
  }
]
```

## 17. Upload/Update UPI QR (Admin)
`PUT /api/admin/payment-settings/upi-qr`

Form-data:
- `qrImage` (file, required): `upi-qr.png`
- `shopUpiName` (text, optional): `Digital Cyber Seva Main`
- `shopUpiId` (text, optional): `shop@upi`

Response:
```json
{
  "upiQrImageUrl": "https://res.cloudinary.com/.../digital-cyber-seva/payment-settings/upi-qr/sample.png",
  "shopUpiName": "Digital Cyber Seva Main",
  "shopUpiId": "shop@upi"
}
```

## 18. Create Category (Admin)
`POST /api/admin/categories`

Request:
```json
{
  "name": "Licenses",
  "iconName": "card-outline",
  "displayOrder": 4,
  "active": true
}
```

Response:
```json
{
  "id": 4,
  "name": "Licenses",
  "iconName": "card-outline",
  "displayOrder": 4,
  "active": true
}
```

## 19. Update Category (Admin)
`PUT /api/admin/categories/{id}`

Request/response same shape as create.

## 20. Create Service (Admin)
`POST /api/admin/services`

Request:
```json
{
  "categoryId": 1,
  "title": "Birth Certificate",
  "description": "Apply for birth certificate",
  "requiredDocumentsJson": "[\"Hospital Slip\",\"Parent Aadhaar\"]",
  "govtFee": 35.00,
  "serviceFee": 65.00,
  "status": "OPEN",
  "openDate": "2026-04-28",
  "closeDate": null
}
```

Response: `ServiceResponse`.

## 21. Update Service (Admin)
`PUT /api/admin/services/{id}`

Request/response same shape as create.

## 22. List All Requests (Admin)
`GET /api/admin/requests`

Response: Array of `ApplicationRequestResponse`.

## 23. Get Request by ID (Admin)
`GET /api/admin/requests/{id}`

Response: `ApplicationRequestResponse`.

## 24. Update Request Status (Admin)
`PUT /api/admin/requests/{id}/status`

Request:
```json
{
  "status": "IN_PROGRESS",
  "remarks": "Documents verified"
}
```

Response: `ApplicationRequestResponse`.

## 25. Upload Final Document (Admin)
`POST /api/admin/requests/{id}/final-document`

Form-data:
- `file` (file): `final-certificate.pdf`

Response: `ApplicationRequestResponse` with status `COMPLETED`.

## 26. Verify Payment (Admin)
`PUT /api/admin/requests/{id}/payment/verify`

Request:
```json
{
  "status": "PAID",
  "remarks": "UPI proof verified manually"
}
```

Response: `ApplicationRequestResponse`.
