package org.hiahatf.mass.service.monero;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.FundingState;
import org.hiahatf.mass.models.lightning.InvoiceLookupResponse;
import org.hiahatf.mass.models.lightning.InvoiceState;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.SwapResponse;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.models.monero.multisig.SweepAllResponse;
import org.hiahatf.mass.models.monero.multisig.SweepAllResult;
import org.hiahatf.mass.models.monero.transfer.TransferResponse;
import org.hiahatf.mass.models.monero.transfer.TransferResult;
import org.hiahatf.mass.models.monero.validate.GetAddressResponse;
import org.hiahatf.mass.models.monero.validate.GetAddressResult;
import org.hiahatf.mass.models.monero.validate.IsMultisigResponse;
import org.hiahatf.mass.models.monero.validate.IsMultisigResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResult;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.monero.SwapService;
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
    ScheduledExecutorService scheduledExecutorService;
    @Mock
    MassUtil massUtil;
    @Mock
    MoneroQuoteRepository quoteRepository;
    @Mock
    Lightning lightning;
    @Mock
    Monero monero;
    @Mock
    ResponseEntity<Void> entity;
    @InjectMocks
    SwapService swapService = new SwapService(quoteRepository, lightning, monero, massUtil,
        "test", "54testrpaddress");
    
    @Test
    @DisplayName("Monero Swap Service Test")
    public void processMoneroSwapTest() throws SSLException, IOException {
        String txset = "expectedTxset";
        SwapRequest swapRequest = SwapRequest.builder()
            .hash("hash").build();
        Optional<XmrQuoteTable> table = Optional.of(XmrQuoteTable.builder()
        .amount(0.1)
        .payment_hash(new byte[32])
        .quote_id("qid")
        .dest_address("54xxx")
        .funding_state(FundingState.COMPLETE)
        .build());
        SweepAllResult result = SweepAllResult.builder()
            .multisig_txset(txset)
            .build();
        SweepAllResponse sweepAllResponse = SweepAllResponse.builder()
            .result(result)
            .build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        // mocks
        when(quoteRepository.findById(swapRequest.getHash())).thenReturn(table);
        when(monero.controlWallet(WalletState.OPEN, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.sweepAll(table.get().getDest_address()))
            .thenReturn(Mono.just(sweepAllResponse));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(table.get(), true)).thenReturn(Mono.just(entity));
        Mono<SwapResponse> testRes = swapService.transferMonero(swapRequest);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getMultisigTxSet()
          .equals(txset))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Cancel Swap Test")
    public void cancelSwapTest() throws SSLException, IOException {
        SwapRequest swapRequest = SwapRequest.builder()
            .hash("hash").build();
        Optional<XmrQuoteTable> table = Optional.of(XmrQuoteTable.builder()
        .amount(0.1)
        .payment_hash(new byte[32])
        .quote_id("qid")
        .dest_address("54xxx")
        .funding_state(FundingState.COMPLETE)
        .build());
        SweepAllResponse sweepAllResponse= SweepAllResponse.builder()
            .result(null)
            .build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        // mocks
        when(quoteRepository.findById(swapRequest.getHash())).thenReturn(table);
        when(monero.controlWallet(WalletState.OPEN, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.sweepAll(table.get().getDest_address()))
            .thenReturn(Mono.just(sweepAllResponse));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(table.get(), false)).thenReturn(Mono.just(entity));
        
        Mono<SwapResponse> testRes = swapService.transferMonero(swapRequest);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r
          .equals(SwapResponse.builder().build()))
        .verifyComplete();
    }
    

    @Test
    @DisplayName("Fund Monero Swap Test")
    public void fundMoneroSwapTest() throws SSLException, IOException {
        String expectedAddress = "54mulitsigaddress";
        String expectedTxId = "txtest123";
        FundRequest fundRequest = FundRequest.builder()
            .hash("hash").build();
        Optional <XmrQuoteTable> table = Optional.of(XmrQuoteTable.builder()
            .amount(0.123).dest_address(expectedAddress)
            .funding_state(FundingState.PENDING)
            .funding_txid("0xfundtxid")
            .mediator_filename("mfn").mediator_finalize_msig("mfmsig")
            .quote_id("lnbcrtquoteid")
            .swap_address(expectedAddress)
            .swap_finalize_msig("sfmisg").build());
        FundResponse fundResponse = FundResponse.builder()
            .importMediatorMultisigInfo("importMediatorMultisigInfo")
            .importMediatorMultisigInfo("importMediatorMultisigInfo")
            .build();
        TransferResult transferResult = TransferResult.builder()
            .tx_hash(expectedTxId).amount(123L)
            .fee(1L).build();
        TransferResponse transferResponse = TransferResponse.builder()
            .result(transferResult).build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        IsMultisigResult isMultisigResult = IsMultisigResult.builder()
            .multisig(true).ready(true)
            .threshold(Constants.MULTISIG_THRESHOLD).total(Constants.MULTISIG_TOTAL)
            .build();
        IsMultisigResponse isMultisigResponse = IsMultisigResponse.builder()
            .result(isMultisigResult).build();
        GetAddressResult getAddressResult = GetAddressResult.builder()
            .address(expectedAddress).build();
        GetAddressResponse getAddressResponse = GetAddressResponse.builder()
            .result(getAddressResult).build();
        InvoiceLookupResponse invoiceLookupResponse = InvoiceLookupResponse
            .builder()
            .state(InvoiceState.ACCEPTED)
            .build();
        // mocks
        when(quoteRepository.findById(anyString())).thenReturn(table);
        when(massUtil.exportSwapInfo(fundRequest, table.get())).thenReturn(Mono.just(fundResponse));
        when(monero.controlWallet(WalletState.OPEN, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(lightning.lookupInvoice(table.get().getQuote_id()))
            .thenReturn(Mono.just(invoiceLookupResponse));
        when(monero.isMultisig()).thenReturn(Mono.just(isMultisigResponse));
        when(monero.getAddress()).thenReturn(Mono.just(getAddressResponse));
        when(monero.transfer(table.get().getSwap_address(), table.get().getAmount()))
            .thenReturn(Mono.just(transferResponse));
        
        Mono<FundResponse> testResponse = swapService.fundMoneroSwap(fundRequest);
        
        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r.getTxid()
          .equals(expectedTxId))
        .verifyComplete();
    }

}
