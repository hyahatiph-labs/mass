package org.hiahatf.mass.service.rpc;

import java.io.IOException;

import javax.net.ssl.SSLException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.HttpHeaders;

import org.hiahatf.mass.models.lightning.AddHoldInvoiceResponse;
import org.hiahatf.mass.models.lightning.Amount;
import org.hiahatf.mass.models.lightning.Info;
import org.hiahatf.mass.models.lightning.InvoiceLookupResponse;
import org.hiahatf.mass.models.lightning.InvoiceState;
import org.hiahatf.mass.models.lightning.Liquidity;
import org.hiahatf.mass.models.lightning.PaymentRequest;
import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.hiahatf.mass.services.rpc.Lightning;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import io.netty.handler.codec.http.HttpHeaderValues;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for Lightning RPC Service
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class LightningTest {
    
    public static MockWebServer mockBackEnd;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Lightning lightning;
    private String testMacaroonPath = 
        "src/test/resources/test.macroon";

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", 
            mockBackEnd.getPort());
        lightning = new Lightning(baseUrl, testMacaroonPath);
    }
    
    @Test
    @DisplayName("Get Info Test")
    public void getInfoTest() throws JsonProcessingException, IOException,
    SSLException {
        String version = "v.0.0.0-test";
        Info info = Info.builder().version(version).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(info))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<Info> testRes = lightning.getInfo();

        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getVersion()
          .equals(version))
        .verifyComplete();
    }

    @Test
    @DisplayName("Lookup Invoice Test")
    public void lookupInvoiceTest() throws JsonProcessingException, IOException,
    SSLException {
        InvoiceLookupResponse res = InvoiceLookupResponse.builder()
            .state(InvoiceState.ACCEPTED).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(res))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<InvoiceLookupResponse> testRes = lightning.lookupInvoice("hash");

        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getState()
          .equals(InvoiceState.ACCEPTED))
        .verifyComplete();
    }

    @Test
    @DisplayName("Generate Invoice Test")
    public void generateInvoiceTest() throws JsonProcessingException, IOException,
    SSLException {
        String expectedPayReq = "lntest";
        Double amount = 0.1;
        byte[] hash = new byte[32];
        AddHoldInvoiceResponse res = AddHoldInvoiceResponse.builder()
            .payment_request(expectedPayReq).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(res))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<AddHoldInvoiceResponse> testRes = lightning.generateInvoice(amount, hash);

        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getPayment_request()
          .equals(expectedPayReq))
        .verifyComplete();
    }

    @Test
    @DisplayName("Handle Invoice Test")
    public void handleInvoiceTest() throws JsonProcessingException, IOException,
    SSLException {
        MoneroQuote quote = MoneroQuote.builder()
            .amount(0.1)
            .quote_id("qid")
            .build();
        SwapRequest swapRequest = SwapRequest.builder().hash("hash").preimage(new byte[32]).build();
        mockBackEnd.enqueue(new MockResponse()
            .setResponseCode(HttpStatus.OK.value())
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<ResponseEntity<Void>> testRes = lightning.handleInvoice(swapRequest, quote, true);

        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getStatusCode()
          .equals(HttpStatus.OK))
        .verifyComplete();
    }

    @Test
    @DisplayName("Fetch Balance Test")
    public void fetchBalanceTest() throws JsonProcessingException, IOException,
    SSLException {
        Amount amount = Amount.builder().msat("10000").sat("10").build();
        Liquidity liquidity = Liquidity.builder()
            .local_balance(amount).remote_balance(amount).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(liquidity))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<Liquidity> testRes = lightning.fetchBalance();
        

        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getLocal_balance()
          .equals(amount))
        .verifyComplete();
    }

    @Test
    @DisplayName("Decode Payment Request Test")
    public void decodePaymentRequestTest() throws JsonProcessingException, IOException,
    SSLException {
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .expiry("600").num_satoshis("100000").payment_hash("hash").build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(paymentRequest))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<PaymentRequest> testRes = lightning.decodePaymentRequest("lntest");
        
        StepVerifier.create(testRes)
        .expectNextMatches(pr -> pr.getPayment_hash()
          .equals(paymentRequest.getPayment_hash()))
        .verifyComplete();
    }

}
