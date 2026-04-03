-- ========================================
-- Company Service endpoint-ready seed data
-- ========================================
-- Covers entity graph used by repositories/services/controllers:
-- address -> stock_exchange -> stock/company -> stock_price
-- sector -> company -> ipo

-- ========================================
-- Cleanup (child tables first)
-- ========================================
DELETE FROM stock_price;
DELETE FROM ipo;
DELETE FROM stock;
DELETE FROM company;
DELETE FROM stock_exchange;
DELETE FROM address;
DELETE FROM sector;

-- ========================================
-- Master data
-- ========================================
INSERT INTO address (id, street, city, country, zipcode) VALUES
(1, 'Dalal Street', 'Mumbai', 'India', 400001),
(2, 'Bandra Kurla Complex', 'Mumbai', 'India', 400051),
(3, 'Lyons Range', 'Kolkata', 'India', 700001);

INSERT INTO stock_exchange (id, name, brief, remarks, address_id) VALUES
(1, 'Bombay Stock Exchange', 'Legacy exchange in India', 'Oldest exchange in Asia', 1),
(2, 'National Stock Exchange', 'High-volume electronic exchange', 'Primary derivatives market', 2),
(3, 'Calcutta Stock Exchange', 'Regional exchange', 'Used for regional listings', 3);

INSERT INTO sector (id, name, brief) VALUES
(1, 'Technology', 'Software and IT services'),
(2, 'Banking', 'Banking and financial services'),
(3, 'Energy', 'Oil, gas and power businesses');

-- ========================================
-- Company data (used by /api/companies/* and IPO/Stock mappings)
-- ========================================
INSERT INTO company (id, name, turnover, ceo, bod, brief, sector_id, is_deleted) VALUES
(1, 'Reliance Industries', 950000000000, 'Mukesh Ambani', 'Mukesh Ambani, Nita Ambani', 'Diversified energy and retail business', 3, false),
(2, 'Tata Consultancy Services', 230000000000, 'K Krithivasan', 'N Chandrasekaran, K Krithivasan', 'Global IT services leader', 1, false),
(3, 'HDFC Bank', 170000000000, 'Sashidhar Jagdishan', 'Atanu Chakraborty, Shyam Srinivasan', 'Large private banking institution', 2, false),
(4, 'Infosys', 155000000000, 'Salil Parekh', 'Nandan Nilekani, Nilanjan Roy', 'Technology consulting and digital services', 1, false);

-- ========================================
-- Stock mappings (critical for findCompanyByExchangeId and stock-price queries)
-- ========================================
-- Keep company_id + stock_exchange_id combinations unique per row because
-- StockPriceRepository subquery expects a single stock id.
INSERT INTO stock (id, stock_code, company_id, stock_exchange_id) VALUES
(1, 'RELIANCE-BSE', 1, 1),
(2, 'TCS-BSE', 2, 1),
(3, 'HDFCBANK-BSE', 3, 1),
(4, 'INFY-BSE', 4, 1),
(5, 'RELIANCE-NSE', 1, 2),
(6, 'TCS-NSE', 2, 2),
(7, 'HDFCBANK-NSE', 3, 2),
(8, 'TCS-CSE', 2, 3);

-- ========================================
-- IPO data
-- ========================================
-- Ipo has OneToOne with company and stock_exchange in entity mapping,
-- so each company and exchange is referenced once.
INSERT INTO ipo (id, price_per_share, total_shares, open_datetime, remarks, company_id, stock_exchange_id) VALUES
(1, 620.00, 15000000, '2021-01-20 09:30:00', 'Reliance follow-on public offer', 1, 1),
(2, 875.50, 12000000, '2021-02-15 10:00:00', 'TCS strategic listing event', 2, 2),
(3, 790.25, 9000000, '2021-03-10 09:45:00', 'HDFC Bank expansion tranche', 3, 3);

-- ========================================
-- Stock price series
-- ========================================
-- Multiple rows for same stock support /api/stock-prices/by-company/{companyId}/{exchangeId}/{before}/{after}
INSERT INTO stock_price (id, price, timestamp, stock_id) VALUES
(1, 2488.40, '2026-03-01 09:15:00', 1),
(2, 2502.10, '2026-03-01 10:15:00', 1),
(3, 2520.85, '2026-03-01 11:15:00', 1),
(4, 3910.00, '2026-03-01 09:15:00', 2),
(5, 3932.45, '2026-03-01 10:15:00', 2),
(6, 1950.25, '2026-03-01 09:15:00', 3),
(7, 1965.75, '2026-03-01 10:15:00', 3),
(8, 1522.60, '2026-03-01 09:15:00', 4),
(9, 1535.10, '2026-03-01 10:15:00', 4),
(10, 2490.00, '2026-03-02 09:15:00', 5),
(11, 2515.35, '2026-03-02 10:15:00', 5),
(12, 3925.80, '2026-03-02 09:15:00', 6),
(13, 3958.55, '2026-03-02 10:15:00', 6),
(14, 1948.90, '2026-03-02 09:15:00', 7),
(15, 1961.20, '2026-03-02 10:15:00', 7),
(16, 3898.75, '2026-03-03 09:30:00', 8);
