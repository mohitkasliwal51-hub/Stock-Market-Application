-- Sector Service seed data
-- Purpose: maintain sector catalog and sector-company associations.

INSERT INTO sector (id, name, brief) VALUES
(1, 'Technology', 'Software and IT services companies'),
(2, 'Banking', 'Retail and corporate banking institutions'),
(3, 'Energy', 'Oil, gas and integrated energy businesses'),
(4, 'Healthcare', 'Hospitals and healthcare services')
ON DUPLICATE KEY UPDATE
name = VALUES(name),
brief = VALUES(brief);

INSERT INTO company (id, name, turnover, ceo, brief, bod, sector_id, is_deleted) VALUES
(1, 'Infosys', 155000000000, 'Salil Parekh', 'Technology consulting and digital transformation', 'Nandan Nilekani, Salil Parekh', 1, false),
(2, 'HDFC Bank', 170000000000, 'Sashidhar Jagdishan', 'Private sector banking and lending services', 'Atanu Chakraborty, Sashidhar Jagdishan', 2, false),
(3, 'Reliance Industries', 950000000000, 'Mukesh Ambani', 'Energy, telecom and retail conglomerate', 'Mukesh Ambani, Nita Ambani', 3, false),
(4, 'Apollo Hospitals', 48000000000, 'Suneeta Reddy', 'Integrated healthcare and hospital network', 'Prathap C Reddy, Suneeta Reddy', 4, false)
ON DUPLICATE KEY UPDATE
name = VALUES(name),
turnover = VALUES(turnover),
ceo = VALUES(ceo),
brief = VALUES(brief),
bod = VALUES(bod),
sector_id = VALUES(sector_id),
is_deleted = VALUES(is_deleted);
