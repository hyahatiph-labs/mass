-- Login in to h2 console and run this on first initialization
CREATE TABLE XMR_QUOTE_TABLE (
    quote_id VARCHAR(64) PRIMARY KEY,
    amount FLOAT(30) NOT NULL,
    payment_hash VARCHAR(64) NOT NULL,
    preimage VARCHAR(64) NOT NULL,
    xmr_address VARCHAR(200) NOT NULL
);

CREATE TABLE BTC_QUOTE_TABLE (
    quote_id VARCHAR(64) PRIMARY KEY
    payment_request VARCHAR(200) NOT NULL,
    amount FLOAT(30) NOT NULL,
    refund_address VARCHAR(200) NOT NULL
); 