DROP TABLE IF EXISTS MONERO_QUOTE;  
CREATE TABLE MONERO_QUOTE (  
quote_id VARCHAR(100) PRIMARY KEY,  
amount FLOAT(100) NOT NULL,  
xmr_address VARCHAR(200) NOT NULL,
invoice VARCHAR(200) NOT NULL,
fulfilled BOOLEAN
);  