# Microservices Architecture - Database Initialization

## 🎯 Overview
This document explains the database initialization strategy for the Stock Market Application microservices architecture.

## 📋 Microservices & Database Responsibility

### **Company Service (Port: 8084)**
- **Database:** `stockApplication` (shared)
- **Tables Managed:**
  - ✅ `company` - Company information
  - ✅ `stock_exchange` - Stock exchange data
  - ✅ `sector` - Industry sector data
  - ✅ `stock` - Stock listings
  - ✅ `ipo` - Initial public offerings
  - ✅ `stock_price` - Historical price data

### **Exchange Service (Port: 8082)**
- **Database:** `stockApplication` (shared)
- **Tables Managed:**
  - 🔄 `stock_exchange` - Read-only access
  - 🔄 `company` - Read-only access
  - 🔄 `stock` - Exchange-specific stock data

### **Sector Service (Port: 8083)**
- **Database:** `stockApplication` (shared)
- **Tables Managed:**
  - 🔄 `sector` - Read-only access
  - 🔄 `company` - Sector-specific companies

### **Excel Service (Port: 8085)**
- **Database:** `stockApplication` (shared)
- **Tables Managed:**
  - 🔄 `stock_price` - Read-only access
  - 🔄 `company` - Company data for Excel operations

### **API Gateway (Port: 8080)**
- **Database:** None (routing service)
- **Tables Managed:** None (delegates to services)

### **Config Service (Port: 8090)**
- **Database:** None (configuration service)
- **Tables Managed:** None (provides configuration)

### **Eureka Service (Port: 8761)**
- **Database:** None (service registry)
- **Tables Managed:** None (service discovery)

## 🎯 Database Initialization Strategy

### **✅ Recommended Approach: Service-Specific Initialization**

Each microservice should only initialize its own tables:

#### **Company Service (Port: 8084)**
```sql
-- Initialize only company-service tables
INSERT INTO company (...) VALUES (...);
INSERT INTO stock_exchange (...) VALUES (...);
INSERT INTO sector (...) VALUES (...);
INSERT INTO stock (...) VALUES (...);
INSERT INTO ipo (...) VALUES (...);
INSERT INTO stock_price (...) VALUES (...);
```

#### **Exchange Service (Port: 8082)**
```sql
-- Initialize only exchange-service tables
-- Note: Reads from shared tables, no inserts needed
```

#### **Sector Service (Port: 8083)**
```sql
-- Initialize only sector-service tables
-- Note: Reads from shared tables, no inserts needed
```

#### **Excel Service (Port: 8085)**
```sql
-- Initialize only excel-service tables
-- Note: Reads from shared tables, no inserts needed
```

## 🚀 Current Implementation

### **Company Service - ✅ Properly Implemented**
- ✅ **data.sql** with comprehensive sample data
- ✅ **DataInitializer.java** with programmatic approach
- ✅ **Environment profiles** (dev/prod)
- ✅ **Microservice boundaries** respected

### **Other Services - 🔄 Need Implementation**
- 🔄 **Exchange Service:** Should initialize exchange-specific data
- 🔄 **Sector Service:** Should initialize sector-specific data
- 🔄 **Excel Service:** Should initialize Excel processing data

## 📊 Database Schema Overview

### **Shared Database: `stockApplication`**

```sql
-- Core Tables (Company Service Domain)
CREATE TABLE company (
    id INT PRIMARY KEY AUTO_INCREMENT,
    company_name VARCHAR(255) NOT NULL,
    turnover DECIMAL(15,2),
    ceo VARCHAR(255),
    board_of_directors TEXT,
    brief_writeup TEXT,
    sector_id INT,
    active BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (sector_id) REFERENCES sector(id)
);

CREATE TABLE stock_exchange (
    id INT PRIMARY KEY AUTO_INCREMENT,
    stock_exchange_name VARCHAR(255) NOT NULL,
    brief TEXT,
    address VARCHAR(500),
    remarks TEXT
);

CREATE TABLE sector (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sector_name VARCHAR(255) NOT NULL,
    brief TEXT
);

CREATE TABLE stock (
    id INT PRIMARY KEY AUTO_INCREMENT,
    stock_code VARCHAR(50) NOT NULL,
    company_id INT NOT NULL,
    stock_exchange_id INT NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id),
    FOREIGN KEY (stock_exchange_id) REFERENCES stock_exchange(id)
);

CREATE TABLE ipo (
    id INT PRIMARY KEY AUTO_INCREMENT,
    price_per_share DECIMAL(10,2) NOT NULL,
    total_shares INT NOT NULL,
    open_datetime TIMESTAMP NOT NULL,
    remarks TEXT,
    company_id INT NOT NULL,
    stock_exchange_id INT NOT NULL,
    FOREIGN KEY (company_id) REFERENCES company(id),
    FOREIGN KEY (stock_exchange_id) REFERENCES stock_exchange(id)
);

CREATE TABLE stock_price (
    id INT PRIMARY KEY AUTO_INCREMENT,
    price DECIMAL(10,2) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    stock_id INT NOT NULL,
    FOREIGN KEY (stock_id) REFERENCES stock(id)
);
```

## 🎯 Best Practices

### **✅ Service Boundaries**
- Each service owns its domain tables
- Shared tables are read by multiple services
- No duplicate data initialization
- Clear separation of concerns

### **✅ Data Consistency**
- Foreign key relationships maintained
- Proper data types used
- Realistic sample data provided

### **✅ Environment Management**
- Development: Full initialization with sample data
- Production: No automatic initialization
- Profile-based configuration

## 🚀 Next Steps

### **Phase 1: Exchange Service**
1. Create `exchange-service/data.sql`
2. Initialize exchange-specific data
3. Add Exchange-specific sample companies

### **Phase 2: Sector Service**
1. Create `sector-service/data.sql`
2. Initialize sector-specific data
3. Add sector-specific sample companies

### **Phase 3: Excel Service**
1. Create `excel-service/data.sql`
2. Initialize Excel processing data
3. Add sample Excel data

### **Phase 4: Integration Testing**
1. Test cross-service data access
2. Verify foreign key relationships
3. Validate API responses

## 📋 Summary

**Company Service** ✅ Complete with comprehensive sample data
**Other Services** 🔄 Need service-specific initialization
**Architecture** ✅ Proper microservices boundaries maintained
**Database** ✅ Shared with clear ownership patterns

This approach ensures each microservice maintains its own data while respecting the shared database architecture.
