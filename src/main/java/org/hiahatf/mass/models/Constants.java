package org.hiahatf.mass.models;

/**
 * Global constants. Primarily used to centralize
 * string literals and application.yml values.
 */
public final class Constants {

    // controller values
    public static final String HEALTH_PATH = "/health";
    public static final String XMR_QUOTE_PATH = "/quote/xmr";
    public static final String XMR_SWAP_FINAL_PATH = "/swap/xmr";
    public static final String XMR_SWAP_INIT_PATH = "/swap/initialize/xmr";
    public static final String BTC_QUOTE_PATH = "/quote/btc";
    public static final String BTC_SWAP_FINAL_PATH = "/swap/btc";
    public static final String BTC_SWAP_INIT_PATH = "/swap/finalize/btc";
    
    // model values
    public static final String MEMO = "mass";
    public static final String EXPIRY = "1800";
    public static final String XMR_RPC_VER = "2.0";
    public static final String XMR_RPC_ID = "0";
    public static final String XMR_RPC_TRANSFER = "transfer";
    public static final String XMR_RPC_VALIDATE_ADDRESS = "validate_address";
    public static final String XMR_RPC_GET_RESERVE_PROOF = "get_reserve_proof";
    public static final String XMR_RPC_CREATE_WALLET = "create_wallet";
    public static final String XMR_RPC_OPEN_WALLET = "open_wallet";
    public static final String XMR_RPC_CLOSE_WALLET = "close_wallet";
    public static final String XMR_RPC_PREPARE_MSIG = "prepare_multisig";
    public static final String XMR_RPC_MAKE_MSIG = "make_multisig";
    public static final String XMR_RPC_FINALIZE_MSIG = "finalize_multisig";
    public static final String XMR_RPC_IMPORT_MSIG_INFO = "import_multisig_info";
    public static final String XMR_RPC_EXPORT_MSIG_INFO = "export_multisig_info";
    public static final String XMR_RPC_SIGN_MSIG = "sign_multisig";
    public static final String XMR_RPC_SUBMIT_MSIG = "submit_multisig";
    public static final String XMR_RPC_DESCRIBE_TRANSFER = "describe_transfer";
    public static final String XMR_RPC_SWEEP_ALL = "sweep_all";
    
    // rate service values
    public static final String UPDATE_RATE_MSG = "Updating XMR <-> BTC rate";
    public static final String RATE_PATH = "/data/price";
    public static final String RATE_FROM = "fsym";
    public static final String RATE_TO = "tsyms";
    public static final String XMR = "XMR";
    public static final String BTC = "BTC";
    public static final String RATE_HOST = "${host.price}";

    // lightning service values
    public static final String MACAROON_HEADER = "Grpc-Metadata-macaroon";
    public static final String LND_PATH = "${host.lightning}";
    public static final String MACAROON_PATH = "${macaroon-path}";
    public static final String INFO_PATH = "/v1/getinfo";
    public static final String BALANCE_PATH = "/v1/balance/channels";
    public static final String ADD_INVOICE_PATH = "/v2/invoices/hodl";
    public static final String PAYREQ = "payreq";
    public static final String SETTLE = "settle";
    public static final String CANCEL = "cancel";
    public static final String INVOICE = "invoice";
    public static final String INVOICES = "invoices";
    public static final String HASH_PARAM = "{hash}";
    public static final String V1 = "v1";
    public static final String V2 = "v2";

    // monero service values
    public static final String JSON_RPC = "json_rpc";
    public static final String XMR_RPC_PATH = "${host.monero}";
    
    // util values
    public static final String MARKUP = "${markup}";
    public static final String SEMI_COLON_DELIMITER = ":";
    public static final String RIGHT_BRACKET_DELIMITER = "}";
    public static final String PARSE_RATE_MSG = "parsed rate: {} => real rate: {}";

    // quote service values
    public static final String SHA_256 = "SHA-256";
    public static final Long COIN = 100000000L;
    public static final String RP_ADDRESS = "${rp-address}";
    public static final String SEND_ADDRESS = "${send-address}";
    public static final int EXPIRY_LIMIT = 600;
    public static final String MIN_PAY = "${min-pay}";
    public static final String MAX_PAY = "${max-pay}";
    public static final String MASS_WALLET_FILENAME = "${mass-wallet-filename}";

    // swap service values
    public static final long FUNDING_LOCK_TIME = 1200L;
    public static final long MEDIATOR_INTERVENE_TIME = 1800L;

    // error messages
    public static final String UNK_ERROR = "Unknown error occurred";
    public static final String OPEN_INVOICE_ERROR = "Invoice is still open!";
    public static final String SWAP_CANCELLED_ERROR = "Tx failed. Invoice cancelled";
    public static final String FATAL_SWAP_ERROR = "Fatal, swap failure!";
    public static final String INVALID_ADDRESS_ERROR = "Invalid address";
    public static final String LIQUIDITY_ERROR = "Liquidity validation error";
    public static final String PAYMENT_THRESHOLD_ERROR = 
        "Payment threshold error. (min: {0}, max: {1} satoshis)";
    public static final String HASH_ERROR = "Preimage hashing error: {}";
    public static final String RESERVE_PROOF_ERROR = "Reserve proof error";
    public static final String EXPIRY_ERROR = "Expiry limit is 600 seconds";
    public static final String DECODE_ERROR = "Failed to decode payment request";
    public static final String WALLET_ERROR = "Wallet control is busy. Please try agin.";
    public static final String FUNDING_ERROR = "Consensus wallet funding error.";
    public static final String MEDIATOR_ERROR = "Mediator intervention error.";

    // beans
    public static final String BTC_QUOTE_CONTROLLER = "BitcoinQuoteController";
    public static final String BTC_SWAP_CONTROLLER = "BitcoinSwapController";
    public static final String BTC_QUOTE_SERVICE = "BitcoinQuoteService";
    public static final String BTC_SWAP_SERVICE = "BitcoinSwapService";
    public static final String BTC_QUOTE_REPO = "BitcoinQuoteRepository";
    
    public static final String XMR_QUOTE_CONTROLLER = "MoneroQuoteController";
    public static final String XMR_SWAP_CONTROLLER = "MoneroSwapController";
    public static final String XMR_QUOTE_SERVICE = "MoneroQuoteService";
    public static final String XMR_SWAP_SERVICE = "MoneroSwapService";
    public static final String XMR_QUOTE_REPO = "MoneroQuoteRepository";

}
