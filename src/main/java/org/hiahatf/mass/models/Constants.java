package org.hiahatf.mass.models;

/**
 * Global constants. Primarily used to centralize
 * string literals and application.yml values.
 */
public final class Constants {
    
    // properties

    // controller values
    public static final String HEALTH_PATH = "/health";
    public static final String XMR_QUOTE_PATH = "/quote/xmr";
    public static final String XMR_SWAP_PATH = "/swap/xmr";
    
    // model values
    public static final String MEMO = "mass";
    public static final String EXPIRY = "600";
    public static final String XMR_RPC_VER = "2.0";
    public static final String XMR_RPC_ID = "0";
    public static final String XMR_RPC_TRANSFER = "transfer";
    public static final String XMR_RPC_VALIDATE_ADDRESS = "validate_address";
    
    // service values
    public static final String UPDATE_RATE_MSG = "Updating XMR <-> BTC rate";
    public static final String RATE_PATH = "/data/price";
    public static final String RATE_FROM = "fsym";
    public static final String RATE_TO = "tsyms";
    public static final String XMR = "XMR";
    public static final String BTC = "BTC";
    public static final String RATE_HOST = "${host.price}";
    public static final String MACAROON_HEADER = "Grpc-Metadata-macaroon";
    public static final String LND_PATH = "${host.lightning}";
    public static final String MACAROON_PATH = "${macaroon-path}";
    public static final String INFO_PATH = "/v1/getinfo";
    public static final String BALANCE_PATH = "/v1/balance/channels";
    public static final String ADD_INVOICE_PATH = "/v2/invoices/hodl";
    public static final String SETTLE = "settle";
    public static final String CANCEL = "cancel";
    public static final String INVOICE = "invoice";
    public static final String INVOICES = "invoices";
    public static final String HASH_PARAM = "{hash}";
    public static final String V1 = "v1";
    public static final String V2 = "v2";
    public static final String JSON_RPC = "json_rpc";
    public static final String XMR_RPC_PATH = "${host.monero}";
    public static final String MARKUP = "${markup}";
    public static final String SEMI_COLON_DELIMITER = ":";
    public static final String RIGHT_BRACKET_DELIMITER = "}";
    public static final String PARSE_RATE_MSG = "parsed rate: {} => real rate: {}";
    public static final String INVALID_ADDRESS = "Invalid address";
    public static final String LIQUIDITY_ERROR = "Liquidity error";
    public static final String HASH_ERROR_MSG = "Preimage hashing error: {}";
    public static final String SHA_256 = "SHA-256";
    public static final Long COIN = 100000000L;
    public static final String MIN_PAY = "${min-pay}";
    public static final String MAX_PAY = "${max-pay}";

    // error messages
    public static final String UNK_ERROR_MSG = "Unknown error occurred";
    public static final String OPEN_INVOICE_ERROR_MSG = "Invoice is still open!";
    public static final String SWAP_CANCELLED_ERR_MSG = "Tx failed. Invoice cancelled";
    public static final String FATAL_SWAP_ERROR_MSG = "Fatal, swap failure!";

}
