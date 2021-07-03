IF NOT EXISTS CREATE TABLE MONERO_QUOTE (  
    quote_id VARCHAR(100) PRIMARY KEY,  
    amount INT(30) NOT NULL,
    xmr_address VARCHAR(200) NOT NULL,
    payment_hash VARCHAR(32),
    preimage VARCHAR(32) NOT NULL,
    fulfilled BOOLEAN
);  