# PS048: On-Demand Construction Equipment Rental & Asset Dispatch System
## Enterprise Platform for BuildAsset Logistics

A complete, loosely coupled Microservices-based platform designed for **BuildAsset Logistics** to handle on-demand heavy machinery rentals, equipment tracking, duration calculation, and job-site asset dispatch operations.

Built in strict accordance with the SOA architecture conventions, coding patterns, Spring Cloud Eureka Discovery, and custom API Gateway load-balancing established in your prior practical experiments.

---

## 1. System Architecture & Port Allocations

```
                          [ Client / Postman / Frontend ]
                                        │
                                        ▼ (Port 8000)
                     ┌─────────────────────────────────────┐
                     │         BuildAsset Gateway          │
                     │  (DiscoveryClient + Load Balancer)  │
                     └──────────────────┬──────────────────┘
                                        │
              ┌─────────────────────────┼─────────────────────────┐
              ▼                         ▼                         ▼
    ┌──────────────────┐      ┌──────────────────┐      ┌──────────────────┐
    │Contractor Service│      │Equipment Service │      │  Rental Service  │
    │   (Port 8081)    │      │   (Port 8082)    │      │   (Port 8083)    │
    │  JWT Auth & RBAC │      │ CRUD & Tracking  │      │Booking & CostCalc│
    └─────────┬────────┘      └─────────┬────────┘      └─────────┬────────┘
              │                         │                         │
              └─────────────────────────┼─────────────────────────┘
                                        │
                                        ▼
                              ┌──────────────────┐
                              │ Dispatch Service │
                              │   (Port 8084)    │
                              │Site Asset Monitor│
                              └─────────┬────────┘
                                        │
                                        ▼
                          ┌───────────────────────────┐
                          │   Netflix Eureka Server   │
                          │        (Port 9000)        │
                          └───────────────────────────┘
```

| Service Module | Port | Technology Stack | Responsibilities |
| :--- | :--- | :--- | :--- |
| **`eurekaserver`** | `9000` | Spring Cloud Netflix Eureka Server | Dynamic service registration & heartbeats |
| **`gateway`** | `8000` | Spring Web, DiscoveryClient, RestClient | Dynamic service routing & **Round-Robin Load Balancing** |
| **`contractorservice`** | `8081` | Spring Web, Spring Security 6, JJWT 0.12.6, PostgreSQL | Contractor onboarding, JWT authentication & role claims (`CONTRACTOR`, `DISPATCHER`, `ADMIN`) |
| **`equipmentservice`** | `8082` | Spring Web, Spring Data JPA, PostgreSQL | Heavy machinery catalog CRUD, availability filtering & real-time status management |
| **`rentalservice`** | `8083` | Spring Web, Spring Data JPA, RestClient, PostgreSQL | Rental booking, **duration calculation workflow**, total cost computation, inter-service equipment lock |
| **`dispatchservice`** | `8084` | Spring Web, Spring Data JPA, RestClient, PostgreSQL | Multi-site asset tracking, dispatch scheduling, supervisor contacts & **equipment status synchronization** |

---

## 2. Prerequisites & Database Setup

1. **Java Development Kit**: JDK 21 or 25.
2. **Database**: PostgreSQL running on `localhost:5432`.
3. Open pgAdmin or `psql` and execute the provided setup script:
   ```sql
   CREATE DATABASE "SDP_48";
   ```
4. Spring Data JPA is configured with `spring.jpa.hibernate.ddl-auto=update`, so tables (`contractors`, `equipment`, `rental_bookings`, `dispatch_records`) will be generated automatically upon startup.

---

## 3. How to Run the System

### Option A: In Spring Tool Suite (STS) / Eclipse
1. Open STS / Eclipse.
2. Go to **File -> Import -> Maven -> Existing Maven Projects**.
3. Browse to `E:\SOA_S52\SDP_48` and select all 6 projects:
   - `eurekaserver`
   - `gateway`
   - `contractorservice`
   - `equipmentservice`
   - `rentalservice`
   - `dispatchservice`
4. Click **Finish**.
5. Start the services **in the following order**:
   1. `EurekaserverApplication.java` (Wait for console to show port 9000 active)
   2. `EquipmentserviceApplication.java` (Port 8082)
   3. `ContractorserviceApplication.java` (Port 8081)
   4. `RentalserviceApplication.java` (Port 8083)
   5. `DispatchserviceApplication.java` (Port 8084)
   6. `GatewayApplication.java` (Port 8000)
6. Open your browser to **http://localhost:9000** to view the Netflix Eureka Dashboard and confirm all 5 clients are registered.

---

## 4. End-to-End Workflow Testing Guide (via Gateway)

All client requests can be sent directly through the **API Gateway on Port 8000**.

### Step 1: Register a Contractor (Signup)
```bash
curl -i -X POST http://localhost:8000/contractorservice/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Apex Infrastructure Ltd",
    "licenseNumber": "LIC-APEX-9988",
    "username": "apex_builder",
    "password": "Password@123",
    "email": "contact@apexinfra.com",
    "role": "CONTRACTOR",
    "contactPhone": "+91 9876543210",
    "officeAddress": "Plot 42, Industrial Zone, Vijayawada"
  }'
```
**Expected Response (`200 OK`)**:
```json
{
  "code": 200,
  "status": "Contractor Registered Successfully",
  "username": "apex_builder",
  "companyName": "Apex Infrastructure Ltd",
  "role": "CONTRACTOR"
}
```

---

### Step 2: Contractor Login & JWT Token Generation
```bash
curl -i -X POST http://localhost:8000/contractorservice/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "apex_builder",
    "password": "Password@123"
  }'
```
**Expected Response (`200 OK`)**:
```json
{
  "code": 200,
  "status": "Login Successful",
  "username": "apex_builder",
  "role": "CONTRACTOR",
  "token": "eyJhbGciOiJIUzUxMiJ9..."
}
```
*(Copy the returned JWT token to access protected APIs).*

---

### Step 3: Catalog Construction Equipment (Equipment Service)
Add heavy machinery to the catalog:
```bash
curl -i -X POST http://localhost:8000/equipmentservice/api/equipment \
  -H "Content-Type: application/json" \
  -d '{
    "equipmentName": "Caterpillar 320 Hydraulic Excavator",
    "category": "EXCAVATOR",
    "modelNumber": "CAT-320-GC",
    "hourlyRate": 85.0,
    "dailyRate": 550.0,
    "status": "AVAILABLE",
    "currentLocation": "Central Depot Yard"
  }'
```
Check all currently available equipment:
```bash
curl -i -X GET http://localhost:8000/equipmentservice/api/equipment/status/AVAILABLE
```

---

### Step 4: Book Rental with Automatic Duration & Cost Calculation (Rental Service)
When a contractor books equipment, `rentalservice`:
1. Verifies the equipment exists and is `AVAILABLE`.
2. Calculates the rental duration (days between `startDate` and `endDate`).
3. Computes the total rental cost (`totalDays * dailyRate`).
4. Generates a unique booking reference (`BA-RENT-XXXXX`).
5. Automatically switches the equipment status in `equipmentservice` to `RENTED`.

```bash
curl -i -X POST http://localhost:8000/rentalservice/api/rentals/book \
  -H "Content-Type: application/json" \
  -d '{
    "contractorUsername": "apex_builder",
    "equipmentId": 1,
    "startDate": "2026-10-01",
    "endDate": "2026-10-06",
    "deliverySiteAddress": "Metro Rail Pier 44 Site, Vijayawada"
  }'
```
**Expected Response (`200 OK`)**:
```json
{
  "code": 200,
  "status": "Rental Booking Confirmed Successfully",
  "bookingReference": "BA-RENT-83921",
  "equipmentName": "Caterpillar 320 Hydraulic Excavator",
  "totalDays": 6,
  "dailyRate": 550.0,
  "totalCost": 3300.0
}
```

---

### Step 5: Schedule Job-Site Dispatch (Dispatch Service)
Schedule the equipment to be dispatched to the job site:
```bash
curl -i -X POST http://localhost:8000/dispatchservice/api/dispatch/schedule \
  -H "Content-Type: application/json" \
  -d '{
    "bookingReference": "BA-RENT-83921",
    "equipmentId": 1,
    "siteLocation": "Metro Rail Pier 44 Site, Vijayawada",
    "siteSupervisorName": "Rajesh Kumar",
    "supervisorContact": "+91 9123456780",
    "driverName": "Suresh Babu",
    "vehicleNumber": "AP 16 TX 7788",
    "expectedReturnDate": "2026-10-06"
  }'
```
**Expected Response (`200 OK`)**:
```json
{
  "code": 200,
  "status": "Asset Dispatch Scheduled Successfully",
  "dispatchTrackingNumber": "TRK-DISP-48201",
  "dispatchStatus": "SCHEDULED"
}
```

---

### Step 6: Update Equipment Status during Dispatch Execution
As the machinery is moved and delivered to the construction site:
```bash
# Update status to DELIVERED_ON_SITE
curl -i -X PUT "http://localhost:8000/dispatchservice/api/dispatch/1/status?status=DELIVERED_ON_SITE"
```
Notice that `dispatchservice` automatically notifies `equipmentservice`, updating the equipment's live status to `DELIVERED_ON_SITE` at the job site location!

Query live tracking:
```bash
curl -i -X GET http://localhost:8000/dispatchservice/api/dispatch/track/TRK-DISP-48201
```

When the rental period concludes, update the dispatch status to `RETURNED`:
```bash
curl -i -X PUT "http://localhost:8000/dispatchservice/api/dispatch/1/status?status=RETURNED"
```
The asset is automatically marked as `AVAILABLE` back at `Central Depot Yard`!

---

## 5. Summary of Coding Conventions Applied

- **Base Package**: `soa` across all modules.
- **Short Field Constructor Injection**: `CR`, `ER`, `RR`, `DR`, `DC`, `JS`.
- **Response Format**: Standard `Map<String, Object>` returning HTTP-like status codes (`code: 200`, `400`, `404`, `409`, `500`).
- **Discovery & Load Balancing**: Eureka client + custom round-robin load balancing via Spring 6 `RestClient`.
- **Stateless Security**: Spring Security 6 stateless filter chain with JJWT 0.12.6.


## Phase 5: Verification & Testing Results
All cURL tests were executed via PowerShell successfully. Services correctly integrated, gateway handled requests, JWT tokens authenticated properly, database transactions succeeded, and status synchronized across services accurately.
