package org.hiahatf.mass.service.monero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.lightning.InvoiceLookupResponse;
import org.hiahatf.mass.models.lightning.InvoiceState;
import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.SwapResponse;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.models.monero.transfer.TransferResponse;
import org.hiahatf.mass.models.monero.transfer.TransferResult;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.monero.SwapService;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for Monero Swap Service
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class SwapServiceTest {

    @Mock
    MoneroQuoteRepository quoteRepository;
    @Mock
    Lightning lightning;
    @Mock
    Monero monero;
    @Mock
    ResponseEntity<Void> entity;
    @InjectMocks
    SwapService swapService;
    
    @Test
    @DisplayName("Monero Swap Service Test")
    public void processMoneroSwapTest() throws SSLException, IOException {
        String metadata = "expectedMetadata000";
        SwapRequest swapRequest = SwapRequest.builder()
            .hash("hash").build();
        Optional<XmrQuoteTable> table = Optional.of(XmrQuoteTable.builder()
        .amount(0.1)
        .payment_hash(new byte[32])
        .preimage(new byte[32])
        .quote_id("qid")
        .xmr_address("54xxx")
        .build());
        InvoiceLookupResponse invoiceLookupResponse = InvoiceLookupResponse
            .builder()
            .state(InvoiceState.ACCEPTED)
            .build();
        TransferResult result = TransferResult.builder()
            .tx_metadata(metadata)
            .build();
        TransferResponse transferResponse = TransferResponse.builder()
            .result(result)
            .build();
            
        // mocks
        when(quoteRepository.findById(swapRequest.getHash())).thenReturn(table);
        when(lightning.lookupInvoice(table.get().getQuote_id()))
            .thenReturn(Mono.just(invoiceLookupResponse));
        when(monero.transfer(table.get().getXmr_address(), table.get().getAmount()))
            .thenReturn(Mono.just(transferResponse));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(table.get(), true)).thenReturn(Mono.just(entity));
        Mono<SwapResponse> testRes = swapService.processMoneroSwap(swapRequest);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getMetadata()
          .equals(metadata))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Cancel Swap Test")
    public void cancelSwapTest() throws SSLException, IOException {
        String metadata = "expectedMetadata000";
        SwapRequest swapRequest = SwapRequest.builder()
            .hash("hash").build();
        Optional<XmrQuoteTable> table = Optional.of(XmrQuoteTable.builder()
        .amount(0.1)
        .payment_hash(new byte[32])
        .preimage(new byte[32])
        .quote_id("qid")
        .xmr_address("54xxx")
        .build());
        InvoiceLookupResponse invoiceLookupResponse = InvoiceLookupResponse
            .builder()
            .state(InvoiceState.ACCEPTED)
            .build();
        TransferResponse transferResponse = TransferResponse.builder()
            .result(null)
            .build();
            
        // mocks
        when(quoteRepository.findById(swapRequest.getHash())).thenReturn(table);
        when(lightning.lookupInvoice(table.get().getQuote_id()))
            .thenReturn(Mono.just(invoiceLookupResponse));
        when(monero.transfer(table.get().getXmr_address(), table.get().getAmount()))
            .thenReturn(Mono.just(transferResponse));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(table.get(), false)).thenReturn(Mono.just(entity));
        
        try {
            Mono<SwapResponse> testRes = swapService.processMoneroSwap(swapRequest);
            assertEquals(metadata, testRes.block().getMetadata());
        } catch (Exception e) {
            String expectedError = "org.hiahatf.mass.exception.MassException: " + 
            Constants.SWAP_CANCELLED_ERROR;
            String actualError = e.getMessage();
            assertEquals(expectedError, actualError);
        }
        
    }

}
