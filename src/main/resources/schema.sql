-- Login in to h2 console and verify creation on first app initialization
CREATE TABLE XMR_QUOTE_TABLE (
    quote_id VARCHAR(64) PRIMARY KEY,
    amount FLOAT(30) NOT NULL,
    dest_address VARCHAR(200) NOT NULL,
    funding_txid VARCHAR(200),
    funding_state VARCHAR(20),
    mediator_filename VARCHAR(100) NOT NULL,
    payment_hash VARCHAR(64) NOT NULL,
    swap_filename VARCHAR(100) NOT NULL,
    swap_address VARCHAR(200) NOT NULL
);

CREATE TABLE BTC_QUOTE_TABLE (
    quote_id VARCHAR(64) PRIMARY KEY,
    payment_request VARCHAR(300) NOT NULL,
    amount FLOAT(30) NOT NULL,
    refund_address VARCHAR(200) NOT NULL
);   
