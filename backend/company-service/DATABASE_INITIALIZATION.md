# Database Initialization Guide

## 🎯 Overview
This document explains how to initialize the Company Service database with sample data for development and testing.

## 📁 Available Data

### Companies (5)
- **Reliance Industries** - Technology conglomerate
- **Tata Consultancy Services** - IT services leader
- **HDFC Bank** - Major banking institution
- **Infosys** - Global IT giant
- **Wipro** - IT consulting company

### Stock Exchanges (3)
- **Bombay Stock Exchange (BSE)** - Premier exchange
- **National Stock Exchange (NSE)** - Largest exchange
- **Calcutta Stock Exchange (CSE)** - Regional exchange

### Sectors (4)
- **Technology** - IT & Software Services
- **Banking** - Financial Services
- **Healthcare** - Pharma & Medical
- **Energy** - Oil, Gas & Power

### Stocks (8)
- Multiple stock listings across different exchanges
- Proper company-exchange relationships

### IPOs (3)
- Major company IPOs with realistic data
- Price ranges from ₹605 to ₹880 per share

### Stock Prices (16)
- Real-time price data over 2 days
- Price movements for all major stocks

## 🚀 Initialization Methods

### Method 1: SQL Script (Recommended)
```bash
# Run with SQL initialization
mvn spring-boot:run
```

**Features:**
- ✅ Quick setup with `data.sql`
- ✅ Automatic table creation
- ✅ Sample data ready immediately
- ✅ Easy to modify SQL directly

### Method 2: Programmatic (Advanced)
```bash
# Run with Java initializer
mvn spring-boot:run -Dspring.profiles.active=dev
```

**Features:**
- ✅ Smart data checking (no duplicates)
- ✅ Complex relationship handling
- ✅ Conditional initialization
- ✅ Detailed logging
- ✅ Easy to extend/customize

## 📋 Environment Profiles

### Development (`application-dev.properties`)
```properties
spring.sql.init.mode=always
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

### Production (`application-prod.properties`)
```properties
spring.sql.init.mode=never
spring.jpa.show-sql=false
```

## 🎯 API Testing with Sample Data

Once initialized, you can test all endpoints:

### Companies
```bash
# Get all companies
GET http://localhost:8084/api/companies

# Get specific company
GET http://localhost:8084/api/companies/1

# Create new company
POST http://localhost:8084/api/companies
```

### IPOs
```bash
# Get all IPOs
GET http://localhost:8084/api/ipos

# Get IPO by company
GET http://localhost:8084/api/ipos/by-company/1
```

### Stocks
```bash
# Get all stocks
GET http://localhost:8084/api/stocks

# Create new stock
POST http://localhost:8084/api/stocks
```

### Stock Prices
```bash
# Get all stock prices
GET http://localhost:8084/api/stock-prices

# Get stock prices by company
GET http://localhost:8084/api/stock-prices/by-company/1/1/2024-01-01/2024-01-02
```

## 📊 Swagger Documentation

Access the interactive API documentation:
```
http://localhost:8084/swagger-ui.html
```

All sample data will be visible and testable through Swagger UI!

## 🔧 Customization

### Adding New Data
1. **SQL Method:** Edit `data.sql`
2. **Programmatic:** Modify `DataInitializer.java`

### Disabling Initialization
1. **Production:** Use `prod` profile
2. **Manual:** Set `spring.sql.init.mode=never`

## 🎉 Benefits

- ✅ **Immediate Testing** - Data ready on startup
- ✅ **Consistent Environment** - Same data across team
- ✅ **API Development** - Real data to test against
- ✅ **Demo Ready** - Perfect for presentations
- ✅ **Learning** - Understand data relationships
