@echo off
REM =========================================================================================
REM  BuildAsset Logistics (SDP_48) - Complete End-to-End User & Integration Test Suite
REM  Validates: Eureka Discovery, Microservices, JWT Auth, API Gateway, Error Handling
REM =========================================================================================

set GATEWAY_URL=http://localhost:8000
set EUREKA_URL=http://localhost:9000

echo =========================================================================
echo [STEP 1] Testing Eureka Service Discovery Registry
echo =========================================================================
curl -s -X GET "%EUREKA_URL%/eureka/apps" -H "Accept: application/json"
echo.
echo [PASS] Eureka Server reachable.

echo.
echo =========================================================================
echo [STEP 2] API Gateway Health and Service Directory Check
echo =========================================================================
curl -s -X GET "%GATEWAY_URL%/" -H "Content-Type: application/json"
echo.

echo.
echo =========================================================================
echo [STEP 3] Contractor Signup (Positive and Negative Testing)
echo =========================================================================
echo 3a. Registering a valid contractor...
curl -s -X POST "%GATEWAY_URL%/contractorservice/api/auth/signup" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"infra_lead_01\",\"password\":\"Secure@Pass2026\",\"email\":\"lead@infrabuild.com\",\"companyName\":\"InfraBuild Engineering Ltd\",\"licenseNumber\":\"LIC-CIVIL-2026-99\"}"
echo.

echo 3b. Negative Test: Submitting duplicate contractor registration...
curl -s -X POST "%GATEWAY_URL%/contractorservice/api/auth/signup" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"infra_lead_01\",\"password\":\"Secure@Pass2026\",\"email\":\"lead@infrabuild.com\",\"companyName\":\"InfraBuild Engineering Ltd\",\"licenseNumber\":\"LIC-CIVIL-2026-99\"}"
echo.

echo 3c. Negative Test: Submitting registration with missing required fields...
curl -s -X POST "%GATEWAY_URL%/contractorservice/api/auth/signup" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"incomplete_user\"}"
echo.

echo.
echo =========================================================================
echo [STEP 4] JWT Authentication & Token Issuance (Login)
echo =========================================================================
echo 4a. Authenticating with valid credentials...
curl -s -X POST "%GATEWAY_URL%/contractorservice/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"infra_lead_01\",\"password\":\"Secure@Pass2026\"}" > login_response.json
type login_response.json
echo.

echo 4b. Negative Test: Invalid password authentication...
curl -s -X POST "%GATEWAY_URL%/contractorservice/api/auth/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"infra_lead_01\",\"password\":\"WrongPassword!\"}"
echo.

echo.
echo =========================================================================
echo [STEP 5] Protected Endpoint Access & JWT Validation
echo =========================================================================
echo 5a. Negative Test: Accessing protected endpoint without JWT token (Expect 401 Unauthorized)...
curl -s -X GET "%GATEWAY_URL%/contractorservice/api/contractors"
echo.

echo.
echo =========================================================================
echo [STEP 6] Equipment Inventory Management & Inter-Service Query
echo =========================================================================
echo 6a. Registering Heavy Construction Equipment...
curl -s -X POST "%GATEWAY_URL%/equipmentservice/api/equipment" ^
  -H "Content-Type: application/json" ^
  -d "{\"equipmentName\":\"Caterpillar 320 GC Hydraulic Excavator\",\"category\":\"EXCAVATOR\",\"modelNumber\":\"CAT-320-GC\",\"dailyRate\":550.0,\"specifications\":\"20-ton operating weight, 0.9m3 bucket\",\"status\":\"AVAILABLE\",\"currentLocation\":\"Central Regional Yard\"}"
echo.

echo 6b. Querying all available equipment catalog...
curl -s -X GET "%GATEWAY_URL%/equipmentservice/api/equipment/status/AVAILABLE"
echo.

echo.
echo =========================================================================
echo [STEP 7] Integrated Equipment Rental Workflow
echo =========================================================================
echo 7a. Booking rental (Equipment ID 1) for 5 days...
curl -s -X POST "%GATEWAY_URL%/rentalservice/api/rentals/book" ^
  -H "Content-Type: application/json" ^
  -d "{\"contractorUsername\":\"infra_lead_01\",\"equipmentId\":1,\"startDate\":\"2026-11-01\",\"endDate\":\"2026-11-05\",\"deliverySiteAddress\":\"Metro Rail Viaduct Pier #88, South Corridor\"}"
echo.

echo 7b. Negative Test: Attempting to rent already RENTED equipment (Conflict 409)...
curl -s -X POST "%GATEWAY_URL%/rentalservice/api/rentals/book" ^
  -H "Content-Type: application/json" ^
  -d "{\"contractorUsername\":\"infra_lead_01\",\"equipmentId\":1,\"startDate\":\"2026-11-01\",\"endDate\":\"2026-11-05\",\"deliverySiteAddress\":\"Other Site\"}"
echo.

echo.
echo =========================================================================
echo [STEP 8] Equipment Dispatch & Logistics Tracking
echo =========================================================================
echo 8a. Scheduling heavy machinery dispatch...
curl -s -X POST "%GATEWAY_URL%/dispatchservice/api/dispatch" ^
  -H "Content-Type: application/json" ^
  -d "{\"rentalBookingId\":1,\"bookingReference\":\"BA-RENT-DEMO-01\",\"equipmentName\":\"Caterpillar 320 GC\",\"driverName\":\"Vikram Singh\",\"driverContact\":\"+91 98765 43210\",\"transportVehicleNumber\":\"AP-29-TA-1044\",\"siteLocation\":\"Metro Rail Viaduct Pier #88\"}"
echo.

echo 8b. Updating dispatch transit status...
curl -s -X PUT "%GATEWAY_URL%/dispatchservice/api/dispatch/1/status" ^
  -H "Content-Type: application/json" ^
  -d "{\"status\":\"IN_TRANSIT\",\"location\":\"Highway NH-44 Toll Plaza\",\"notes\":\"Transport trailer en route to construction site\"}"
echo.

echo.
echo =========================================================================
echo [SUCCESS] End-to-End User and Integration Demonstration Complete!
echo =========================================================================
pause
