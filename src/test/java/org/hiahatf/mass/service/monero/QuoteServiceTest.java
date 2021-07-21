package org.hiahatf.mass.service.monero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.lightning.AddHoldInvoiceResponse;
import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.models.monero.proof.GetProofResult;
import org.hiahatf.mass.models.monero.proof.GetReserveProofResponse;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResponse;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResult;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.monero.QuoteService;
import org.hiahatf.mass.services.rate.RateService;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for Monero Quote Service
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class QuoteServiceTest {
    
    @Mock
    private RateService rateService;
    @Mock
    private MassUtil util;
    @Mock
    private Monero moneroRpc;
    @Mock
    private Lightning lightning;
    @Mock
    private MassUtil massUtil;
    @Mock
    private MoneroQuoteRepository quoteRepository;
    private final Long minPay = 10000L;
    private final Long maxPay = 1000000L;
    @InjectMocks
    private QuoteService quoteService = new QuoteService(rateService, massUtil,
    moneroRpc, lightning, quoteRepository, minPay, maxPay, "54rpvxxx");

    @Test
    @DisplayName("Monero Quote Service Test")
    public void processQuoteTest() throws SSLException, IOException {
        String prs = "proofresultsigxxx";
        // build test data
        Request req = Request.builder().address("54xxx")
            .amount(0.1).build();
        GetProofResult getProofResult = GetProofResult.builder()
            .signature(prs).build();
        GetReserveProofResponse reserveProof = GetReserveProofResponse
            .builder().result(getProofResult).build();
        ValidateAddressResult validateAddressResult = ValidateAddressResult.builder()
            .valid(true).build();
        ValidateAddressResponse validateAddressResponse = ValidateAddressResponse.builder()
            .result(validateAddressResult).build();
        AddHoldInvoiceResponse addHoldInvoiceResponse = AddHoldInvoiceResponse.builder()
            .payment_request("lntest123xxx").build();
        // mocks
        when(rateService.getMoneroRate()).thenReturn(Mono.just("{BTC: 0.00777}"));
        when(massUtil.parseMoneroRate(anyString())).thenReturn(0.008);
        when(massUtil.validateInboundLiquidity(anyDouble())).thenReturn(Mono.just(true));
        when(moneroRpc.getReserveProof(req.getAmount())).thenReturn(Mono.just(reserveProof));
        when(moneroRpc.validateAddress(req.getAddress()))
            .thenReturn(Mono.just(validateAddressResponse));
        when(lightning.generateInvoice(any(), any())).thenReturn(Mono.just(addHoldInvoiceResponse));
        Mono<Quote> testQuote = quoteService.processMoneroQuote(req);
        
        StepVerifier.create(testQuote)
        .expectNextMatches(r -> r.getReserveProof().getSignature()
          .equals(prs))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Swap Reserve Proof Error Test")
    public void reserveProofErrorTest() throws SSLException, IOException {
        // build test data
        Request req = Request.builder().address("54xxx")
            .amount(0.1).build();
        GetReserveProofResponse reserveProof = GetReserveProofResponse
            .builder().result(null)
            .build();
        // mocks
        when(rateService.getMoneroRate()).thenReturn(Mono.just("{BTC: 0.00777}"));
        when(massUtil.parseMoneroRate(anyString())).thenReturn(0.008);
        when(massUtil.validateInboundLiquidity(anyDouble())).thenReturn(Mono.just(true));
        when(moneroRpc.getReserveProof(req.getAmount())).thenReturn(Mono.just(reserveProof));
        try {
            Quote test = quoteService.processMoneroQuote(req).block();
            assertNotNull(test);
        } catch (Exception e) {
            String expectedError = "org.hiahatf.mass.exception.MassException: " + 
                "Reserve proof error";
            assertEquals(expectedError, e.getMessage());
        }
    }

}
