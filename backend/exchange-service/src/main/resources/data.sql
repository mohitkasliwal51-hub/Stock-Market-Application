-- Exchange Service seed data
-- Purpose: maintain stock exchange master data and address details.

INSERT INTO address (id, street, city, country, zipcode) VALUES
(1, 'Dalal Street', 'Mumbai', 'India', 400001),
(2, 'Bandra Kurla Complex', 'Mumbai', 'India', 400051),
(3, 'Ring Road', 'Bengaluru', 'India', 560001)
ON DUPLICATE KEY UPDATE
street = VALUES(street),
city = VALUES(city),
country = VALUES(country),
zipcode = VALUES(zipcode);

INSERT INTO stock_exchange (id, name, brief, remarks, address_id) VALUES
(1, 'Bombay Stock Exchange', 'Legacy exchange in India', 'Oldest stock exchange in Asia', 1),
(2, 'National Stock Exchange', 'Electronic exchange for equity and derivatives', 'Primary high-volume exchange', 2),
(3, 'Bangalore Exchange Desk', 'Regional stock desk', 'Supports regional listings', 3)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
brief = VALUES(brief),
remarks = VALUES(remarks),
address_id = VALUES(address_id);
