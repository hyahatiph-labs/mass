package org.hiahatf.mass.services.rpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.lightning.AddHoldInvoiceRequest;
import org.hiahatf.mass.models.lightning.AddHoldInvoiceResponse;
import org.hiahatf.mass.models.lightning.CancelInvoiceRequest;
import org.hiahatf.mass.models.lightning.Info;
import org.hiahatf.mass.models.lightning.InvoiceLookupResponse;
import org.hiahatf.mass.models.lightning.Liquidity;
import org.hiahatf.mass.models.lightning.SettleInvoiceRequest;
import org.hiahatf.mass.models.monero.XmrQuoteTable;

import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

/**
 * Class for performing LND API calls
 */
@Service
public class Lightning {
    
    private String lndHost;
    private String macaroonPath;

    /**
     * Lightning RPC constructor
     * @param host
     * @param macaroonPath
     */
    public Lightning(
        @Value(Constants.LND_PATH) String host,
        @Value(Constants.MACAROON_PATH) String macaroonPath) {
            this.lndHost = host;
            this.macaroonPath = macaroonPath;
    }

    /**
     * Testing LND connectivity
     * @returns Mono<String>
     */
    public Mono<Info> getInfo() throws IOException {
        // lightning rpc web client
        WebClient client = createClient();
        return client.get()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.INFO_PATH).build())
            .header(Constants.MACAROON_HEADER, createMacaroonHex())
            .retrieve()
            .bodyToMono(Info.class);
    }

    /**
     * Lookup the invoice status. MASS is only concerned that
     * the invoice is in the proper state of ACCEPTED.
     * @param hash - hold invoice payment hash
     * @return - InvoiceState
     * @throws SSLException
     * @throws IOException
     */
    public Mono<InvoiceLookupResponse> lookupInvoice(String hash) 
        throws SSLException, IOException {
            WebClient client = createClient();
            return client.get()
                .uri(uriBuilder -> uriBuilder
                .pathSegment(Constants.V1, Constants.INVOICE, Constants.HASH_PARAM)
                .build(hash))
                .header(Constants.MACAROON_HEADER, createMacaroonHex())
                .retrieve()
                .bodyToMono(InvoiceLookupResponse.class);
    }

    /**
     * Generate a hold invoice to settle in the future once the
     * swap has been initiated. The payment request from LND gets
     * injected into the quote based on the amount of Monero
     * requested in the swap.
     * @param value - amount of the swap in satoshis
     * @param hash - hash of hold invoice preimage
     * @return AddHoldInvoiceResponse
     * @throws SSLException
     * @throws IOException
     */
    public Mono<AddHoldInvoiceResponse> generateInvoice(Double value, byte[] hash) 
        throws SSLException, IOException {
            AddHoldInvoiceRequest request = AddHoldInvoiceRequest.builder()
                .value(String.valueOf(value.intValue()))
                .hash(hash)
                .build();
            WebClient client = createClient();
            return client.post()
                .uri(uriBuilder -> uriBuilder
                .path(Constants.ADD_INVOICE_PATH).build())
                .header(Constants.MACAROON_HEADER, createMacaroonHex())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AddHoldInvoiceResponse.class);
    }

    /**
     * Settle the hold invoice with the preimage.
     * If the invoice is not open the the call will
     * succeed. 
     * @param preimage - hold invoice preimage
     * @param settle - flag to drive settle or cancel logic
     * @throws SSLException
     * @throws IOException
     */
    public Mono<ResponseEntity<Void>> handleInvoice(XmrQuoteTable quote, boolean settle)
        throws SSLException, IOException {
            String path = settle ? Constants.SETTLE : Constants.CANCEL;
            SettleInvoiceRequest settleReq = SettleInvoiceRequest
                .builder().preimage(quote.getPreimage()).build();
            CancelInvoiceRequest cancelReq = CancelInvoiceRequest
                .builder().payment_hash(quote.getPayment_hash()).build();
            WebClient client = createClient();
            return client.post()
                .uri(uriBuilder -> uriBuilder
                .pathSegment(Constants.V2, Constants.INVOICES, path)
                .build())
                .header(Constants.MACAROON_HEADER, createMacaroonHex())
                .bodyValue(settle ? settleReq : cancelReq)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(WebClientResponseException.class, 
                e -> e.getRawStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()
                ? Mono.error(new MassException(Constants.OPEN_INVOICE_ERROR_MSG))
                : Mono.error(new MassException(Constants.UNK_ERROR_MSG)));
    }

    /**
     * Make a request to the LND API for retrieving the local and 
     * remote balances.
     * @return Mono<Liquidity>
     * @throws SSLException
     * @throws IOException
     */
    public Mono<Liquidity> fetchBalance() throws SSLException, IOException {
        WebClient client = createClient();
        return client.get()
            .uri(uriBuilder -> uriBuilder
            .path(Constants.BALANCE_PATH)
            .build())
            .header(Constants.MACAROON_HEADER, createMacaroonHex())
            .retrieve()
            .bodyToMono(Liquidity.class);
    }
    
    /**
     * Create the SSL Context for working with 
     * LND self-signed certificate
     * @return HttpClient
     * @throws SSLException
     */
    private HttpClient createSslContext() throws SSLException {
        // work around for the self-signed TLS cert
        SslContext sslContext = SslContextBuilder
            .forClient()
            .trustManager(InsecureTrustManagerFactory.INSTANCE)
            .build();
        return HttpClient.create().secure(t -> t.sslContext(sslContext));
    }

    /**
     * Convert the LND Macaroon
     * @return String - macaroon hex-encoded
     * @throws IOException
     */
    private String createMacaroonHex() throws IOException {
        Path path = Paths.get(macaroonPath);
        byte[] byteArray = Files.readAllBytes(path);
        return Hex.encodeHexString(byteArray);
    }

    /**
     * Helper method for the LND WebClient.
     * Creates a re-usable service for making
     * Lightning network API calls
     * @return WebClient
     * @throws SSLException
     */
    private WebClient createClient() throws SSLException {
        return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(createSslContext()))
        .baseUrl(lndHost).build();
    }

}
