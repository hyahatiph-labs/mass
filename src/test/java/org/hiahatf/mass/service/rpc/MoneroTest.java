package org.hiahatf.mass.service.rpc;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;

import org.hiahatf.mass.models.monero.Description;
import org.hiahatf.mass.models.monero.multisig.DescribeResponse;
import org.hiahatf.mass.models.monero.multisig.DescribeResult;
import org.hiahatf.mass.models.monero.multisig.ExportInfoResponse;
import org.hiahatf.mass.models.monero.multisig.ExportInfoResult;
import org.hiahatf.mass.models.monero.multisig.FinalizeResponse;
import org.hiahatf.mass.models.monero.multisig.FinalizeResult;
import org.hiahatf.mass.models.monero.multisig.ImportInfoResponse;
import org.hiahatf.mass.models.monero.multisig.ImportInfoResult;
import org.hiahatf.mass.models.monero.multisig.MakeResponse;
import org.hiahatf.mass.models.monero.multisig.MakeResult;
import org.hiahatf.mass.models.monero.multisig.PrepareResponse;
import org.hiahatf.mass.models.monero.multisig.PrepareResult;
import org.hiahatf.mass.models.monero.multisig.SignResponse;
import org.hiahatf.mass.models.monero.multisig.SignResult;
import org.hiahatf.mass.models.monero.multisig.SubmitResponse;
import org.hiahatf.mass.models.monero.multisig.SubmitResult;
import org.hiahatf.mass.models.monero.multisig.SweepAllResponse;
import org.hiahatf.mass.models.monero.multisig.SweepAllResult;
import org.hiahatf.mass.models.monero.proof.GetProofResult;
import org.hiahatf.mass.models.monero.proof.GetReserveProofResponse;
import org.hiahatf.mass.models.monero.transfer.TransferResponse;
import org.hiahatf.mass.models.monero.transfer.TransferResult;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResponse;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
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
import org.springframework.http.HttpHeaders;

import io.netty.handler.codec.http.HttpHeaderValues;
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
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
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
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
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
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<GetReserveProofResponse> testRes = monero.getReserveProof(amount);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult()
          .equals(result))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Create Wallet Test")
    public void createWalletTest() throws JsonProcessingException { 
        String id = "2.0";
        CreateWalletResponse response = CreateWalletResponse.builder().id(id).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<CreateWalletResponse> testRes = monero.createWallet("testFilename");
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getId()
          .equals(id))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Control Wallet Test")
    public void controlWalletTest() throws JsonProcessingException { 
        String id = "2.0";
        WalletState state = WalletState.OPEN;
        WalletStateResponse response = WalletStateResponse.builder().id(id).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<WalletStateResponse> testRes = monero.controlWallet(state, "test");
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getId()
          .equals(id))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Prepare Multisig Test")
    public void prepareMultisigTest() throws JsonProcessingException { 
        String info = "testinfo";
        PrepareResult result = PrepareResult.builder().multisig_info(info).build();
        PrepareResponse response = PrepareResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<PrepareResponse> testRes = monero.prepareMultisig();
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getMultisig_info()
          .equals(info))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Make Multisig Test")
    public void makeMultisigTest() throws JsonProcessingException { 
        String info = "testinfo";
        List<String> infoList = Lists.newArrayList();
        infoList.add("clientInfo");
        MakeResult result = MakeResult.builder().multisig_info(info).build();
        MakeResponse response = MakeResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<MakeResponse> testRes = monero.makeMultisig(infoList);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getMultisig_info()
          .equals(info))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Finalize Multisig Test")
    public void finalizeMultisigTest() throws JsonProcessingException { 
        String address = "54testaddress";
        List<String> infoList = Lists.newArrayList();
        infoList.add("clientInfo");
        FinalizeResult result = FinalizeResult.builder().address(address).build();
        FinalizeResponse response = FinalizeResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<FinalizeResponse> testRes = monero.finalizeMultisig(infoList);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getAddress()
          .equals(address))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Import MultisigInfo Test")
    public void importMultisigInfoTest() throws JsonProcessingException { 
        int outputs = 1;
        List<String> infoList = Lists.newArrayList();
        infoList.add("clientInfo");
        ImportInfoResult result = ImportInfoResult.builder().n_outputs(outputs).build();
        ImportInfoResponse response = ImportInfoResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<ImportInfoResponse> testRes = monero.importMultisigInfo(infoList);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getN_outputs() == outputs)
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Export MultisigInfo Test")
    public void exportMultisigInfoTest() throws JsonProcessingException { 
        String info = "testinfo";
        List<String> infoList = Lists.newArrayList();
        infoList.add("clientInfo");
        ExportInfoResult result = ExportInfoResult.builder().info(info).build();
        ExportInfoResponse response = ExportInfoResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<ExportInfoResponse> testRes = monero.exportMultisigInfo();
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getInfo()
          .equals(info))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Sign Multisig Test")
    public void signMultisigTest() throws JsonProcessingException { 
        String txDataHex = "testdatahex";
        List<String> txHashList = Lists.newArrayList();
        txHashList.add("txHash");
        SignResult result = SignResult.builder()
            .tx_data_hex(txDataHex).tx_hash_list(txHashList).build();
        SignResponse response = SignResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<SignResponse> testRes = monero.signMultisig(txDataHex);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getTx_data_hex()
          .equals(txDataHex))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Submit Multisig Test")
    public void submitMultisigTest() throws JsonProcessingException { 
        String txDataHex = "testdatahex";
        List<String> txHashList = Lists.newArrayList();
        txHashList.add("txHash");
        SubmitResult result = SubmitResult.builder().tx_hash_list(txHashList).build();
        SubmitResponse response = SubmitResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<SubmitResponse> testRes = monero.submitMultisig(txDataHex);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getTx_hash_list()
          .equals(txHashList))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Describe Transfer Test")
    public void describeTransferTest() throws JsonProcessingException { 
        String txSet = "testtxset";
        Description desc = Description.builder().amount_in(123L)
        .amount_out(123L).change_address("54change").change_amount(123L)
        .dummy_outputs(11).fee(123L).build();
        DescribeResult result = DescribeResult.builder().desc(desc).build();
        DescribeResponse response = DescribeResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<DescribeResponse> testRes = monero.describeTransfer(txSet);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getDesc()
          .equals(desc))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Sweep All Test")
    public void sweepAllTest() throws JsonProcessingException { 
        String address = "54testaddress";
        String txSet = "txSet";
        SweepAllResult result = SweepAllResult.builder().multisig_txset(txSet).build();
        SweepAllResponse response = SweepAllResponse.builder().result(result).build();
        mockBackEnd.enqueue(new MockResponse()
            .setBody(objectMapper.writeValueAsString(response))
            .addHeader(HttpHeaders.CONTENT_TYPE, 
                HttpHeaderValues.APPLICATION_JSON.toString()));
        Mono<SweepAllResponse> testRes = monero.sweepAll(address);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getResult().getMultisig_txset()
          .equals(txSet))
        .verifyComplete();
    }

}
