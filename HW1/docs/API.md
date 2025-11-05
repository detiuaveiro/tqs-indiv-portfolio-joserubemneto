# REST API Documentation - ZeroMonos Waste Collection System

## Base URL
```
http://localhost:8080/api
```

## Endpoints Overview

### Public Endpoints (Citizens)

#### 1. Create Service Request
Create a new waste collection request.

**Endpoint:** `POST /requests`

**Request Body:**
```json
{
  "municipalityCode": "LISB01",
  "municipalityName": "Lisboa",
  "citizenName": "João Silva",
  "citizenEmail": "joao@example.com",
  "citizenPhone": "912345678",
  "pickupAddress": "Rua Example, 123, Lisboa",
  "itemDescription": "Old refrigerator and washing machine",
  "preferredDate": "2025-11-15",
  "preferredTimeSlot": "MORNING"
}
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "token": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6",
  "municipalityCode": "LISB01",
  "municipalityName": "Lisboa",
  "citizenName": "João Silva",
  "citizenEmail": "joao@example.com",
  "citizenPhone": "912345678",
  "pickupAddress": "Rua Example, 123, Lisboa",
  "itemDescription": "Old refrigerator and washing machine",
  "preferredDate": "2025-11-15",
  "preferredTimeSlot": "MORNING",
  "status": "RECEIVED",
  "createdAt": "2025-11-05T10:00:00",
  "updatedAt": "2025-11-05T10:00:00",
  "statusHistory": [
    {
      "id": 1,
      "previousStatus": null,
      "newStatus": "RECEIVED",
      "timestamp": "2025-11-05T10:00:00",
      "notes": "Initial request created"
    }
  ]
}
```

**Validations:**
- All fields except `citizenEmail` are required
- `citizenPhone` must be exactly 9 digits
- `citizenEmail` must be valid email format
- `itemDescription` must be between 10 and 500 characters
- `preferredDate` must be in the future
- Daily limit per municipality: 10 active requests

**Time Slots:**
- `MORNING` (08:00 - 12:00)
- `AFTERNOON` (12:00 - 18:00)
- `EVENING` (18:00 - 21:00)

---

#### 2. Get Service Request by Token
Retrieve details of a service request using the access token.

**Endpoint:** `GET /requests/{token}`

**Path Parameters:**
- `token` - Unique access token received when creating the request

**Response:** `200 OK`
```json
{
  "id": 1,
  "token": "a1b2c3d4-e5f6-g7h8-i9j0-k1l2m3n4o5p6",
  "municipalityName": "Lisboa",
  "status": "ASSIGNED",
  "statusHistory": [
    {
      "id": 2,
      "previousStatus": "RECEIVED",
      "newStatus": "ASSIGNED",
      "timestamp": "2025-11-05T11:00:00",
      "notes": "Assigned to team A"
    },
    {
      "id": 1,
      "previousStatus": null,
      "newStatus": "RECEIVED",
      "timestamp": "2025-11-05T10:00:00",
      "notes": "Initial request created"
    }
  ]
}
```

**Error Responses:**
- `404 Not Found` - Token does not exist

---

#### 3. Cancel Service Request
Cancel an existing service request using the token.

**Endpoint:** `DELETE /requests/{token}`

**Path Parameters:**
- `token` - Unique access token

**Response:** `204 No Content`

**Business Rules:**
- Cannot cancel completed requests
- Cannot cancel already cancelled requests

**Error Responses:**
- `404 Not Found` - Token does not exist
- `400 Bad Request` - Cannot cancel (already completed/cancelled)

---

### Staff Endpoints

#### 4. Get All Service Requests
List all service requests with optional municipality filter.

**Endpoint:** `GET /staff/requests`

**Query Parameters:**
- `municipality` (optional) - Filter by municipality name (e.g., `Lisboa`)

**Examples:**
```
GET /staff/requests
GET /staff/requests?municipality=Lisboa
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "token": "...",
    "municipalityName": "Lisboa",
    "citizenName": "João Silva",
    "status": "ASSIGNED",
    "preferredDate": "2025-11-15",
    "preferredTimeSlot": "MORNING",
    "createdAt": "2025-11-05T10:00:00",
    "statusHistory": [...]
  },
  {
    "id": 2,
    "municipalityName": "Porto",
    "status": "RECEIVED",
    ...
  }
]
```

---

#### 5. Update Service Request Status
Update the status of a service request.

**Endpoint:** `PUT /staff/requests/{id}/status`

**Path Parameters:**
- `id` - Service request ID

**Request Body:**
```json
{
  "newStatus": "ASSIGNED",
  "notes": "Assigned to team A"
}
```

**Response:** `200 OK`
```json
{
  "id": 1,
  "status": "ASSIGNED",
  "statusHistory": [
    {
      "previousStatus": "RECEIVED",
      "newStatus": "ASSIGNED",
      "timestamp": "2025-11-05T11:00:00",
      "notes": "Assigned to team A"
    },
    ...
  ]
}
```

**Valid Status Transitions:**
- `RECEIVED` → `ASSIGNED` or `CANCELLED`
- `ASSIGNED` → `IN_PROGRESS` or `CANCELLED`
- `IN_PROGRESS` → `COMPLETED` or `CANCELLED`
- `CANCELLED` → `RECEIVED` (reopen)
- `COMPLETED` → (no transitions allowed)

**Error Responses:**
- `404 Not Found` - Request ID does not exist
- `400 Bad Request` - Invalid status transition

---

### Municipality Endpoints

#### 6. Get All Municipalities
Get list of available municipalities (from GeoAPI.pt).

**Endpoint:** `GET /municipalities`

**Response:** `200 OK`
```json
[
  {
    "name": "Lisboa",
    "code": "LISB01"
  },
  {
    "name": "Porto",
    "code": "PORT02"
  },
  ...
]
```

---

## Error Response Format

All error responses follow this format:

```json
{
  "status": 400,
  "message": "Error message description",
  "timestamp": "2025-11-05T10:00:00"
}
```

**Validation Error Response:**
```json
{
  "status": 400,
  "message": "Validation failed",
  "timestamp": "2025-11-05T10:00:00",
  "errors": {
    "citizenName": "Citizen name is required",
    "preferredDate": "Preferred date must be in the future"
  }
}
```

---

## Status Values

- `RECEIVED` - Request received and pending assignment
- `ASSIGNED` - Request assigned to collection team
- `IN_PROGRESS` - Collection in progress
- `COMPLETED` - Collection completed successfully
- `CANCELLED` - Request cancelled by user or system

---

## Business Rules

1. **Daily Limit:** Maximum 10 active requests per municipality per day
2. **Date Validation:** Collection date must be in the future
3. **Status Transitions:** Must follow the valid transition rules
4. **Token Access:** Citizens can only access their own requests using the token
5. **Cancellation:** Cannot cancel completed requests

---

## cURL Examples

### Create Request
```bash
curl -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -d '{
    "municipalityCode": "LISB01",
    "municipalityName": "Lisboa",
    "citizenName": "João Silva",
    "citizenEmail": "joao@example.com",
    "citizenPhone": "912345678",
    "pickupAddress": "Rua Example, 123",
    "itemDescription": "Old refrigerator and washing machine",
    "preferredDate": "2025-11-15",
    "preferredTimeSlot": "MORNING"
  }'
```

### Get Request by Token
```bash
curl http://localhost:8080/api/requests/{token}
```

### Cancel Request
```bash
curl -X DELETE http://localhost:8080/api/requests/{token}
```

### Get All Municipalities
```bash
curl http://localhost:8080/api/municipalities
```

### Staff: Get All Requests
```bash
curl http://localhost:8080/api/staff/requests
```

### Staff: Update Status
```bash
curl -X PUT http://localhost:8080/api/staff/requests/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "newStatus": "ASSIGNED",
    "notes": "Assigned to team A"
  }'
```

