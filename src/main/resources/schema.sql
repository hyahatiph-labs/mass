-- Login in to h2 console and verify creation on first app initialization
CREATE TABLE XMR_QUOTE_TABLE (
    quote_id VARCHAR(64) PRIMARY KEY,
    amount FLOAT(30) NOT NULL,
    dest_address VARCHAR(200) NOT NULL,
    funding_txid VARCHAR(200),
    mediator_filename VARCHAR(100) NOT NULL,
    mediator_finalize_msig VARCHAR(200) NOT NULL,
    peer_id VARCHAR(64) NOT NULL,
    swap_finalize_msig VARCHAR(200) NOT NULL,
    payment_hash VARCHAR(64) NOT NULL,
    swap_filename VARCHAR(100) NOT NULL,
    swap_address VARCHAR(200) NOT NULL
);

CREATE TABLE BTC_QUOTE_TABLE (
    quote_id VARCHAR(64) PRIMARY KEY,
    amount FLOAT(30) NOT NULL,
    locked_rate FLOAT(30),
    payment_hash VARCHAR(64) NOT NULL,
    peer_id VARCHAR(64) NOT NULL,
    preimage VARCHAR(64) NOT NULL,
    refund_address VARCHAR(200) NOT NULL,
    swap_filename VARCHAR(100) NOT NULL
);   

CREATE TABLE PEER_TABLE (
    peer_id VARCHAR(64) PRIMARY KEY,
    cancel_counter INT,
    is_active BOOLEAN,
    is_vetted BOOLEAN,
    is_malicious BOOLEAN,
    swap_counter INT
);