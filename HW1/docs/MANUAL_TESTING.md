# Manual Testing Guide - ZeroMonos

## 🧪 Test Scenarios

### Scenario 1: Create a Service Request (Happy Path)

**Steps:**
1. Open frontend: http://localhost:5173
2. Click "Create Request" or navigate to `/create`
3. Fill in the form:
   - Municipality: Select "Lisboa"
   - Pickup Address: "Rua Example, 123, 1000-001 Lisboa"
   - Name: "João Silva"
   - Email: "joao@example.com"
   - Phone: "912345678"
   - Item Description: "Old refrigerator and washing machine that need disposal"
   - Preferred Date: Tomorrow's date
   - Time Slot: "Morning"
4. Click "Submit Request"

**Expected Result:**
- ✅ Success message appears
- ✅ Access token is displayed
- ✅ Form is cleared
- ✅ Token should be saved for next steps

---

### Scenario 2: Check Request Status

**Steps:**
1. Navigate to "Check Status" or `/check`
2. Enter the token from Scenario 1
3. Click "Search"

**Expected Result:**
- ✅ Request details are displayed
- ✅ Status shows "RECEIVED"
- ✅ All information matches the input
- ✅ Status history shows 1 entry
- ✅ "Cancel Request" button is visible

---

### Scenario 3: Cancel a Request

**Steps:**
1. From the request details page (Scenario 2)
2. Click "Cancel Request"
3. Confirm cancellation

**Expected Result:**
- ✅ Status updates to "CANCELLED"
- ✅ Status history shows 2 entries
- ✅ "Cancel Request" button disappears

---

### Scenario 4: Validation Errors

#### 4.1 Missing Required Fields
**Steps:**
1. Go to `/create`
2. Try to submit without filling any fields

**Expected Result:**
- ❌ Browser validation prevents submission
- ❌ Fields are highlighted

#### 4.2 Invalid Phone Number
**Steps:**
1. Go to `/create`
2. Enter phone: "123456" (less than 9 digits)
3. Try to submit

**Expected Result:**
- ❌ Validation error: "Phone must be 9 digits"

#### 4.3 Invalid Email
**Steps:**
1. Go to `/create`
2. Enter email: "invalid-email"
3. Try to submit

**Expected Result:**
- ❌ Validation error for email format

#### 4.4 Past Date
**Steps:**
1. Go to `/create`
2. Fill all fields correctly
3. Select yesterday's date
4. Submit

**Expected Result:**
- ❌ Error: "Preferred date must be in the future"

#### 4.5 Description Too Short
**Steps:**
1. Go to `/create`
2. Enter description: "fridge" (less than 10 chars)
3. Submit

**Expected Result:**
- ❌ Browser validation: minimum 10 characters

---

### Scenario 5: Daily Limit Exceeded

**Steps:**
1. Create 10 requests for "Lisboa" with tomorrow's date
2. Try to create 11th request for "Lisboa" with same date

**Expected Result:**
- ❌ Error: "Daily limit reached for municipality Lisboa"

---

### Scenario 6: Invalid Token

**Steps:**
1. Go to `/check`
2. Enter token: "invalid-token-123"
3. Search

**Expected Result:**
- ❌ Error: "Request not found. Please check your token."

---

### Scenario 7: Cannot Cancel Completed Request

**Steps:**
1. Create a request (via API or frontend)
2. Use staff endpoint to change status to COMPLETED:
   ```bash
   curl -X PUT http://localhost:8080/api/staff/requests/1/status \
     -H "Content-Type: application/json" \
     -d '{"newStatus": "COMPLETED", "notes": "Collection done"}'
   ```
3. Try to cancel via frontend using the token

**Expected Result:**
- ❌ Error: "Cannot cancel a completed request"
- ❌ Cancel button not visible

---

## 🔧 API Testing (using cURL)

### Create Request
```bash
curl -X POST http://localhost:8080/api/requests \
  -H "Content-Type: application/json" \
  -d '{
    "municipalityCode": "LISB01",
    "municipalityName": "Lisboa",
    "citizenName": "Test User",
    "citizenEmail": "test@example.com",
    "citizenPhone": "912345678",
    "pickupAddress": "Test Address, 123",
    "itemDescription": "Test items for disposal - refrigerator",
    "preferredDate": "2025-12-01",
    "preferredTimeSlot": "MORNING"
  }'
```

### Get Request by Token
```bash
TOKEN="your-token-here"
curl http://localhost:8080/api/requests/$TOKEN
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

### Cancel Request
```bash
TOKEN="your-token-here"
curl -X DELETE http://localhost:8080/api/requests/$TOKEN
```

---

## 🎯 Status Transition Testing

Valid transitions to test:

1. **RECEIVED → ASSIGNED**
   ```bash
   curl -X PUT http://localhost:8080/api/staff/requests/1/status \
     -H "Content-Type: application/json" \
     -d '{"newStatus": "ASSIGNED", "notes": "Team assigned"}'
   ```

2. **ASSIGNED → IN_PROGRESS**
   ```bash
   curl -X PUT http://localhost:8080/api/staff/requests/1/status \
     -H "Content-Type: application/json" \
     -d '{"newStatus": "IN_PROGRESS", "notes": "Collection started"}'
   ```

3. **IN_PROGRESS → COMPLETED**
   ```bash
   curl -X PUT http://localhost:8080/api/staff/requests/1/status \
     -H "Content-Type: application/json" \
     -d '{"newStatus": "COMPLETED", "notes": "Collection finished"}'
   ```

4. **RECEIVED → CANCELLED** (by citizen or staff)
   ```bash
   curl -X PUT http://localhost:8080/api/staff/requests/1/status \
     -H "Content-Type: application/json" \
     -d '{"newStatus": "CANCELLED", "notes": "Cancelled by staff"}'
   ```

Invalid transitions (should fail):

5. **RECEIVED → COMPLETED** (should fail)
   ```bash
   curl -X PUT http://localhost:8080/api/staff/requests/1/status \
     -H "Content-Type: application/json" \
     -d '{"newStatus": "COMPLETED", "notes": "Invalid"}'
   ```
   Expected: ❌ "Invalid status transition from RECEIVED to COMPLETED"

6. **COMPLETED → anything** (should fail)
   Expected: ❌ "Cannot change status of completed request"

---

## 📊 H2 Database Testing

1. Access H2 Console: http://localhost:8080/h2-console
2. Login with:
   - JDBC URL: `jdbc:h2:mem:wastecollectiondb`
   - User: `sa`
   - Password: _(empty)_

### Useful Queries

```sql
-- View all requests
SELECT * FROM service_requests;

-- View status history
SELECT * FROM status_history ORDER BY timestamp DESC;

-- Count requests by municipality
SELECT municipality_name, COUNT(*) as count 
FROM service_requests 
GROUP BY municipality_name;

-- View requests with status
SELECT id, citizen_name, municipality_name, status, preferred_date 
FROM service_requests 
ORDER BY created_at DESC;
```

---

## ✅ Frontend UI/UX Testing

### Home Page
- [ ] Logo and title display correctly
- [ ] "Create Request" button navigates to `/create`
- [ ] "Check Status" button navigates to `/check`
- [ ] Responsive design (test on mobile width)
- [ ] Steps section is clear and readable

### Create Request Page
- [ ] All form fields are visible and labeled
- [ ] Municipality dropdown populates from API
- [ ] Date picker shows tomorrow as minimum
- [ ] Time slot dropdown has 3 options
- [ ] Character counter updates on item description
- [ ] Phone validation (9 digits)
- [ ] Success message shows token prominently
- [ ] Token is easy to copy
- [ ] Form resets after successful submission

### Check Request Page
- [ ] Token input accepts text
- [ ] Search button triggers request
- [ ] Loading state shows while fetching
- [ ] Request details display in organized sections
- [ ] Status badge has correct color
- [ ] Timeline shows chronologically (newest first)
- [ ] Cancel button appears only for non-completed requests
- [ ] Confirmation dialog shows before cancel
- [ ] Error messages are clear and helpful

### Navigation
- [ ] Navbar links work correctly
- [ ] Logo link returns to home
- [ ] Active page is identifiable
- [ ] Responsive menu on mobile

### Accessibility
- [ ] All form fields have labels
- [ ] Error messages are readable
- [ ] Color contrast is sufficient
- [ ] Keyboard navigation works

---

## 🐛 Edge Cases

1. **Very long descriptions** (500 chars)
2. **Special characters in name/address**
3. **Multiple requests from same citizen**
4. **Weekend/holiday dates**
5. **Concurrent requests** (same municipality, same date)
6. **Browser refresh during form fill**
7. **Network errors** (stop backend mid-request)

---

## 📝 Test Checklist

- [ ] All validation rules work
- [ ] API endpoints return correct responses
- [ ] Status transitions follow rules
- [ ] Error messages are meaningful
- [ ] UI is responsive
- [ ] Token is generated uniquely
- [ ] Status history is tracked
- [ ] Daily limits are enforced
- [ ] External API (municipalities) works
- [ ] Cancel functionality works correctly

