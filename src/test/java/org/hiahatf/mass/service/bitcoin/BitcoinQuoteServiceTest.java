package org.hiahatf.mass.service.bitcoin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.Quote;
import org.hiahatf.mass.models.bitcoin.Request;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.proof.CheckReserveProofResponse;
import org.hiahatf.mass.models.monero.proof.CheckReserveProofResult;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResponse;
import org.hiahatf.mass.models.monero.validate.ValidateAddressResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResult;
import org.hiahatf.mass.repo.BitcoinQuoteRepository;
import org.hiahatf.mass.services.bitcoin.QuoteService;
import org.hiahatf.mass.services.rate.RateService;
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
 * Tests for Bitcoin Quote Service
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class BitcoinQuoteServiceTest {
    
    @Mock
    private BitcoinQuoteRepository bitcoinQuoteRepository;
    @Mock
    private RateService rateService;
    @Mock
    private Monero monero;
    @Mock
    private MassUtil massUtil;
    @InjectMocks
    private QuoteService quoteService = new QuoteService(bitcoinQuoteRepository, 10000L, 100000L, 
        massUtil, rateService, monero, "sendAddy", "test", false);
        
    @Test
    @DisplayName("Bitcoin Quote Service Test")
    public void processBitcoinQuoteTest() {
        Double expectedAmount = 0.1 * Constants.PICONERO;
        // build test data
        List<String> infos = Lists.newArrayList();
        Request req = Request.builder().proofAddress("proofAddress")
            .proofSignature("proofSignature").refundAddress("refundAddress")
            .swapMultisigInfos(infos).amount(0.1).build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        CheckReserveProofResult checkReserveProofResult = CheckReserveProofResult.builder()
            .good(true).total(expectedAmount.longValue()).build();
        CheckReserveProofResponse checkReserveProofResponse = CheckReserveProofResponse.builder()
            .result(checkReserveProofResult).build();
        ValidateAddressResult validateAddressResult = ValidateAddressResult.builder()
            .valid(true).build();
        ValidateAddressResponse validateAddressResponse = ValidateAddressResponse.builder()
            .result(validateAddressResult).build();
        MultisigData data = MultisigData.builder().clientMultisigInfos(req.getSwapMultisigInfos())
            .swapFilename("sfn").mediatorMakeMultisigInfo("mmsi")
            .mediatorFilename("mfn").build();
        // mocks
        when(rateService.getMoneroRate()).thenReturn("{BTC: 0.00777}");
        when(massUtil.parseMoneroRate(anyString())).thenReturn(0.008);
        when(massUtil.validateLiquidity(anyDouble(), any()))
            .thenReturn(Mono.just(true));
        when(monero.controlWallet(WalletState.OPEN, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.checkReserveProof(req.getProofAddress(), req.getProofSignature()))
            .thenReturn(Mono.just(checkReserveProofResponse));
        when(monero.validateAddress(req.getRefundAddress()))
            .thenReturn(Mono.just(validateAddressResponse));
        when(massUtil.rConfigureMultisig(anyList(), anyString())).thenReturn(Mono.just(data));

        Mono<Quote> testQuote = quoteService.processBitcoinQuote(req);

        StepVerifier.create(testQuote)
        .expectNextMatches(r -> r.getSendTo()
          .equals("sendAddy"))
        .verifyComplete();
    }

}
