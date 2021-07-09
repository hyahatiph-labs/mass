package com.hiahatf.mass.services.rpc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.net.ssl.SSLException;

import com.hiahatf.mass.exception.MassException;
import com.hiahatf.mass.models.AddHoldInvoiceRequest;
import com.hiahatf.mass.models.AddHoldInvoiceResponse;
import com.hiahatf.mass.models.SettleInvoiceRequest;

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

@Service("LightningRpc")
public class Lightning {
    
    private String lndHost;
    private String macaroonPath;
    private static final String MACAROON_HEADER = "Grpc-Metadata-macaroon";

    /**
     * Lightning RPC constructor
     * @param host
     * @param macaroonPath
     */
    public Lightning(@Value("${host.lightning}") String host,
    @Value("${macaroon-path}") String macaroonPath) {
        this.lndHost = host;
        this.macaroonPath = macaroonPath;
    }

    /**
     * Testing LND connectivity
     * @returns Mono<String>
     */
    public Mono<String> getInfo() throws IOException {
        // lightning rpc web client
        WebClient client = createClient();
        return client.get()
            .uri(uriBuilder -> uriBuilder
            .path("/v1/getinfo").build())
            .header(MACAROON_HEADER, createMacaroonHex())
            .retrieve()
            .bodyToMono(String.class);
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
    public Mono<AddHoldInvoiceResponse> 
        generateInvoice(Double value, byte[] hash) 
        throws SSLException, IOException {
            AddHoldInvoiceRequest request = AddHoldInvoiceRequest.builder()
                .value(String.valueOf(value.intValue()))
                .hash(hash)
                .build();
            WebClient client = createClient();
            return client.post()
                .uri(uriBuilder -> uriBuilder
                .path("/v2/invoices/hodl").build())
                .header(MACAROON_HEADER, createMacaroonHex())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(AddHoldInvoiceResponse.class);
    }

    /**
     * Settle the hold invoice with the preimage.
     * If the invoice is not open the the call will
     * succeed.
     * @param preimage - hold invoice preimage
     * @throws SSLException
     * @throws IOException
     */
    public Mono<ResponseEntity<Void>> 
        settleInvoice(byte[] preimage) 
        throws SSLException, IOException {
            SettleInvoiceRequest request = SettleInvoiceRequest
                .builder()
                .preimage(preimage)
                .build();
            WebClient client = createClient();
            return client.post()
                .uri(uriBuilder -> uriBuilder
                .path("/v2/invoices/settle")
                .build())
                .header(MACAROON_HEADER, createMacaroonHex())
                .bodyValue(request)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(WebClientResponseException.class, 
                e -> e.getRawStatusCode() == HttpStatus.INTERNAL_SERVER_ERROR.value()
                ? Mono.error(new MassException("Invoice not settled!"))
                : Mono.error(new MassException("Unknown error occurred while attempting to settle the invoice")));
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
