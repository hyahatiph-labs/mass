DROP TABLE IF EXISTS XmrQuoteTable;

CREATE TABLE XmrQuoteTable (  
    quote_id VARCHAR(100) PRIMARY KEY,  
    amount FLOAT(30) NOT NULL,
    xmr_address VARCHAR(200) NOT NULL,
    preimage_hash ARRAY[32] NOT NULL,
    preimage ARRAY[32] NOT NULL,
    fulfilled BOOLEAN NOT NULL
);  