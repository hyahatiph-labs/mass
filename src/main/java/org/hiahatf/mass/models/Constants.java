package org.hiahatf.mass.models;

/**
 * Global constants. Primarily used to centralize
 * string literals and application.yml values.
 */
public final class Constants {

    // controller values
    public static final String HEALTH_PATH = "/health";
    public static final String INTEGRITY_PATH = "/integrity";
    public static final String MASS_RATE_PATH = "/rate";
    public static final String XMR_QUOTE_PATH = "/quote/xmr";
    public static final String XMR_SWAP_FINAL_PATH = "/swap/xmr";
    public static final String XMR_SWAP_FUND_PATH = "/swap/fund/xmr";
    public static final String XMR_SWAP_INIT_PATH = "/swap/initialize/xmr";
    public static final String XMR_CANCEL_PATH = "/swap/cancel/xmr";
    public static final String BTC_QUOTE_PATH = "/quote/btc";
    public static final String BTC_SWAP_FINAL_PATH = "/swap/btc";
    public static final String BTC_SWAP_FUND_PATH = "/swap/fund/btc";
    public static final String BTC_SWAP_INIT_PATH = "/swap/initialize/btc";
    public static final String BTC_CANCEL_PATH = "/swap/cancel/btc";
    public static final String PEER_ADD_PATH = "/peer/add";
    public static final String PEER_VIEW_PATH = "/peer/view";

    // Integrity values
    public static final String XMR_QUOTE_CONTROLLER_SRC = 
        "src/main/java/org/hiahatf/mass/controllers/monero/QuoteController.java";
    public static final String XMR_SWAP_CONTROLLER_SRC = 
        "src/main/java/org/hiahatf/mass/controllers/monero/SwapController.java";
    public static final String XMR_QUOTE_SERVICE_SRC = 
        "src/main/java/org/hiahatf/mass/services/monero/QuoteService.java";
    public static final String XMR_SWAP_SERVICE_SRC = 
        "src/main/java/org/hiahatf/mass/services/monero/SwapService.java";
    public static final String BTC_QUOTE_CONTROLLER_SRC = 
        "src/main/java/org/hiahatf/mass/controllers/bitcoin/QuoteController.java";
    public static final String BTC_SWAP_CONTROLLER_SRC = 
        "src/main/java/org/hiahatf/mass/controllers/bitcoin/SwapController.java";
    public static final String BTC_QUOTE_SERVICE_SRC = 
        "src/main/java/org/hiahatf/mass/services/bitcoin/QuoteService.java";
    public static final String BTC_SWAP_SERVICE_SRC = 
        "src/main/java/org/hiahatf/mass/services/bitcoin/SwapService.java";
    public static final String INTEGRITY_SRC = 
        "src/main/java/org/hiahatf/mass/controllers/IntegrityController.java";
    
    // model values
    public static final String MEMO = "mass";
    public static final String EXPIRY = "7200";
    public static final String XMR_RPC_VER = "2.0";
    public static final String XMR_RPC_ID = "0";
    public static final String XMR_RPC_TRANSFER = "transfer";
    public static final String XMR_RPC_VALIDATE_ADDRESS = "validate_address";
    public static final String XMR_RPC_GET_RESERVE_PROOF = "get_reserve_proof";
    public static final String XMR_RPC_CHECK_RESERVE_PROOF = "check_reserve_proof";
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
    public static final String XMR_RPC_GET_BALANCE = "get_balance";
    
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
    public static final String ROUTER = "router";
    public static final String SEND = "send";
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
    public static final long MULTISIG_WAIT = 60L;
    public static final double PICONERO = 1.0E12;
    
    // util values
    public static final String MARKUP = "${markup}";
    public static final String SEMI_COLON_DELIMITER = ":";
    public static final String RIGHT_BRACKET_DELIMITER = "}";
    public static final String PARSE_RATE_MSG = "parsed rate: {} => real rate: {}";
    public static final String MEDIATOR_CHECK = "MEDIATOR_CHECK";

    // quote service values
    public static final String SHA_256 = "SHA-256";
    public static final Long COIN = 100000000L;
    public static final String RP_ADDRESS = "${rp-address}";
    public static final String SEND_ADDRESS = "${send-address}";
    public static final int EXPIRY_LIMIT = 7200;
    public static final String MIN_PAY = "${min-pay}";
    public static final String MAX_PAY = "${max-pay}";
    public static final String MASS_WALLET_FILENAME = "${mass-wallet-filename}";

    // swap service values
    public static final long MEDIATOR_INTERVENE_TIME = 3600L;
    public static final long MEDIATOR_RETRY_DELAY = 60L;
    public static final int MULTISIG_THRESHOLD = 2;
    public static final int MULTISIG_TOTAL = 3;
    public static final String RATE_LOCK_MODE = "${mode.rate-lock}";
    public static final String PRICE_CONFIDENCE = "${mode.price-confidence}";

    // peer service values

    /** 
      This is a magic number for max peers. I'm too lazy to write up the p2p
      architecture. It is based on n+1 where n equals six degrees of separation.
      Linking peers are n and n+1 become linking peers as the network expands 
      in a fractal pattern.
      TODO: publish diagrams and stuff of the p2p architecture.
    */
    public static final int MAX_PEERS = 7;
    public static final String PEER_PROXY = "http://localhost:4444";
    public static final String BASE32_REGEX = "/^[A-Z2-7]+=*$/";
    public static final String PEER_DISCOVERY_MSG = "Starting peer discovery";
    public static final String PEER_ADDED_MSG = "Adding peer {} from discovery";
    public static final String PEER_UPDATE_MSG = "Starting peer update";
    public static final String PEER_VETTED_MSG = "Vetted peer {}";
    public static final String PEER_MALICIOUS_MSG = "Tagged peer {} as malicious";
    public static final String PEER_ACTIVE_MSG = "Peer {} is active";
    public static final String PEER_INACTIVE_MSG = "Peer {} is inactive";
    public static final String PEER_HOST_FORMAT = "http://{0}.b32.ip";
    public static final String IS_SHARING_PEERS = "${peer.share}";

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
    public static final String EXPIRY_ERROR = "Expiry limit is 7200 seconds";
    public static final String DECODE_ERROR = "Failed to decode payment request.";
    public static final String WALLET_ERROR = "Wallet control is busy, please try agin.";
    public static final String FUNDING_ERROR = "Consensus wallet funding error, {0} block(s) remain";
    public static final String MEDIATOR_ERROR = "Mediator intervention error";
    public static final String MULTISIG_CONFIG_ERROR = "Multisig configuration error";
    public static final String INVALID_AMT_ERROR = "Funding amount is invalid. This could be due to a an unacceptable price fluctuation for a not rate-locked server. Please create a new swap.";
    public static final String MAX_PEER_ERROR = "Max peer error";
    public static final String INVALID_PEER_ERROR = "Invalid base32 peer. Don't send '.b32.i2p'";
    public static final String PEER_SHARE_ERROR = "Peer is not sharing";

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

    public static final String PEER_CONTROLLER = "PeerController";
    public static final String PEER_SERVICE = "PeerService";
    public static final String PEER_REPO = "PeerRepo";

}
