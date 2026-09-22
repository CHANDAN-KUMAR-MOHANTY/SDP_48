-- =====================================================================
-- BuildAsset Logistics (SDP_48) - PostgreSQL Database Setup Script
-- =====================================================================
-- Run this script in pgAdmin, psql, or your preferred PostgreSQL client.

-- 1. Create database (if it doesn't already exist)
CREATE DATABASE "SDP_48";

-- Connect to SDP_48 database:
-- \c SDP_48

-- 2. Schema Table Previews (Spring Data JPA will auto-generate/update these tables on startup)
-- Note: spring.jpa.hibernate.ddl-auto=update is enabled across all microservices.

-- Table: contractors (managed by contractorservice on port 8081)
-- Table: equipment (managed by equipmentservice on port 8082)
-- Table: rental_bookings (managed by rentalservice on port 8083)
-- Table: dispatch_records (managed by dispatchservice on port 8084)

-- 3. Initial Sample Data Seed (Optional: You can insert this after tables are created)

INSERT INTO equipment (equipment_name, category, model_number, hourly_rate, daily_rate, status, current_location)
VALUES 
('Caterpillar 320 Hydraulic Excavator', 'EXCAVATOR', 'CAT-320-GC', 85.0, 550.0, 'AVAILABLE', 'Central Depot Yard'),
('Komatsu PC210LC Crawler Excavator', 'EXCAVATOR', 'PC210LC-11', 75.0, 480.0, 'AVAILABLE', 'Central Depot Yard'),
('Liebherr LTM 11200 Mobile Crane', 'CRANE', 'LTM-11200-9.1', 250.0, 1800.0, 'AVAILABLE', 'Heavy Crane Yard North'),
('CAT D8T Heavy Track Bulldozer', 'BULLDOZER', 'CAT-D8T-WH', 110.0, 750.0, 'AVAILABLE', 'Central Depot Yard'),
('Volvo L250H Wheel Loader', 'LOADER', 'L250H-STAGE-V', 90.0, 600.0, 'AVAILABLE', 'South Depot Yard'),
('Schwing Stetter Concrete Mixer Truck', 'CONCRETE_MIXER', 'AM-6-FHC', 60.0, 390.0, 'AVAILABLE', 'East Fleet Station');
