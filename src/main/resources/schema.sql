-- Login in to h2 console and run this on first initialization
CREATE TABLE XMR_QUOTE_TABLE (
    preimage_hash VARCHAR(64) PRIMARY KEY,
    amount FLOAT(30) NOT NULL,
    preimage VARCHAR(64) NOT NULL,
    xmr_address VARCHAR(200) NOT NULL
);  