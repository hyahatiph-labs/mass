package org.hiahatf.mass.service.rpc;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.hiahatf.mass.models.monero.proof.GetProofResult;
import org.hiahatf.mass.models.monero.proof.GetReserveProofResponse;
import org.hiahatf.mass.models.monero.transfer.TransferResponse;
import org.hiahatf.mass.models.monero.transfer.TransferResult;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResponse;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResult;
import org.hiahatf.mass.services.rpc.Monero;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.junit.jupiter.MockitoExtension;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for Monero RPC Service
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class MoneroTest {

    public static MockWebServer mockBackEnd;
    private ObjectMapper objectMapper = new ObjectMapper();
    private Monero monero;

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
        monero = new Monero(baseUrl);
    }

    @Test
    @DisplayName("Monero Validate Address Test")
    public void validateAddressTest() throws JsonProcessingException {
        String address = "54testAddress";
        ValidateAddressResult result = ValidateAddressResult.builder()
            .valid(true).build();
        ValidateAddressResponse res = ValidateAddressResponse.builder()
            .result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(res))
            .addHeader("Content-Type", "application/json"));
        Mono<ValidateAddressResponse> testRes = monero.validateAddress(address);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult()
          .equals(result))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Transfer Test")
    public void transferTest() throws JsonProcessingException {
        String address = "54testAddress";
        Double amount = 0.1;
        TransferResult result = TransferResult.builder()
            .tx_hash("hash").build();
        TransferResponse response = TransferResponse.builder()
            .result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader("Content-Type", "application/json"));
        Mono<TransferResponse> testRes = monero.transfer(address, amount);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult()
          .equals(result))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Reserve Proof Test")
    public void reserveProofTest() throws JsonProcessingException {
        Double amount = 0.1;
        GetProofResult result = GetProofResult.builder()
            .signature("reserveProofTest").build();
        GetReserveProofResponse response = GetReserveProofResponse.builder()
            .result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader("Content-Type", "application/json"));
        Mono<GetReserveProofResponse> testRes = monero.getReserveProof(amount);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult()
          .equals(result))
        .verifyComplete();
    }

}
