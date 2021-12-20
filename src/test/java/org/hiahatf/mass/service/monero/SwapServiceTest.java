package org.hiahatf.mass.service.monero;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.lightning.InvoiceLookupResponse;
import org.hiahatf.mass.models.lightning.InvoiceState;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.SwapResponse;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.hiahatf.mass.models.monero.balance.BalanceResponse;
import org.hiahatf.mass.models.monero.balance.BalanceResult;
import org.hiahatf.mass.models.monero.multisig.FinalizeResponse;
import org.hiahatf.mass.models.monero.multisig.FinalizeResult;
import org.hiahatf.mass.models.monero.multisig.SignResponse;
import org.hiahatf.mass.models.monero.multisig.SignResult;
import org.hiahatf.mass.models.monero.multisig.SubmitResponse;
import org.hiahatf.mass.models.monero.multisig.SubmitResult;
import org.hiahatf.mass.models.monero.multisig.SweepAllResponse;
import org.hiahatf.mass.models.monero.multisig.SweepAllResult;
import org.hiahatf.mass.models.monero.transfer.TransferResponse;
import org.hiahatf.mass.models.monero.transfer.TransferResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResult;
import org.hiahatf.mass.models.peer.Peer;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.repo.PeerRepository;
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
    PeerRepository peerRepository;
    @Mock
    Lightning lightning;
    @Mock
    Monero monero;
    @Mock
    ResponseEntity<Void> entity;
    @InjectMocks
    SwapService swapService = new SwapService(quoteRepository, lightning, monero, 
        massUtil,"test", "54testrpaddress", peerRepository);
    
    @Test
    @DisplayName("Monero Swap Service Test")
    public void transferMoneroSwapTest() throws SSLException, IOException {
        String txset = "expectedTxset";
        SwapRequest swapRequest = SwapRequest.builder()
            .hash("hash").preimage(new byte[32]).build();
        Optional<Peer> peer = Optional.of(Peer.builder().peer_id("peer_id").build());
        Optional<MoneroQuote> quote = Optional.of(MoneroQuote.builder()
        .amount(0.1)
        .payment_hash(new byte[32])
        .peer_id("peer_id")
        .quote_id("qid")
        .dest_address("54xxx")
        .swap_filename("sfn")
        .mediator_filename("mfn")
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
        BalanceResult balanceResult = BalanceResult.builder()
            .blocks_to_unlock(0).build();
        BalanceResponse balanceResponse = BalanceResponse.builder()
            .result(balanceResult).build();
        // mocks
        when(quoteRepository.findById(swapRequest.getHash())).thenReturn(quote);
        when(monero.controlWallet(WalletState.OPEN, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.getBalance()).thenReturn(Mono.just(balanceResponse));
        when(monero.sweepAll(quote.get().getDest_address()))
            .thenReturn(Mono.just(sweepAllResponse));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(swapRequest, quote.get(), true)).thenReturn(Mono.just(entity));
        when(peerRepository.findById(anyString())).thenReturn(peer);
        Mono<SwapResponse> testRes = swapService.transferMonero(swapRequest);
        
        StepVerifier.create(testRes)
        .expectNextMatches(r -> r.getMultisigTxSet()
          .equals(txset))
        .verifyComplete();
    }

    @Test
    @DisplayName("Monero Swap Sweep Failure Test")
    public void sweepFailSwapTest() throws SSLException, IOException {
        SwapRequest swapRequest = SwapRequest.builder()
            .hash("hash").preimage(new byte[32]).build();
        Optional<MoneroQuote> quote = Optional.of(MoneroQuote.builder()
        .amount(0.1)
        .payment_hash(new byte[32])
        .quote_id("qid")
        .dest_address("54xxx")
        .swap_filename("swap_filename")
        .mediator_filename("mediator_filename")
        .build());
        SweepAllResponse sweepAllResponse= SweepAllResponse.builder()
            .result(null)
            .build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        BalanceResult balanceResult = BalanceResult.builder()
            .blocks_to_unlock(0).build();
        BalanceResponse balanceResponse = BalanceResponse.builder()
            .result(balanceResult).build();
        // mocks
        when(quoteRepository.findById(swapRequest.getHash())).thenReturn(quote);
        when(monero.controlWallet(WalletState.OPEN, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.getBalance()).thenReturn(Mono.just(balanceResponse));
        when(monero.sweepAll(quote.get().getDest_address()))
            .thenReturn(Mono.just(sweepAllResponse));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(swapRequest, quote.get(), false)).thenReturn(Mono.just(entity));
        
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
            .makeMultisigInfo("makeMultisigInfo")
            .hash("hash").build();
        Optional <MoneroQuote> quote = Optional.of(MoneroQuote.builder()
            .amount(0.123).dest_address(expectedAddress)
            .funding_txid("0xfundtxid")
            .mediator_filename("mfn").mediator_finalize_msig("mfmsig")
            .quote_id("lnbcrtquoteid")
            .swap_address(expectedAddress)
            .swap_finalize_msig("sfmisg").build());
        TransferResult transferResult = TransferResult.builder()
            .tx_hash(expectedTxId).amount(123L)
            .fee(1L).build();
        TransferResponse transferResponse = TransferResponse.builder()
            .result(transferResult).build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        InvoiceLookupResponse invoiceLookupResponse = InvoiceLookupResponse
            .builder()
            .state(InvoiceState.ACCEPTED)
            .build();
        FinalizeResult finalizeResult = FinalizeResult.builder()
            .address(expectedAddress).build();
        FinalizeResponse finalizeResponse = FinalizeResponse.builder()
            .result(finalizeResult).build();
        // mocks
        when(quoteRepository.findById(anyString())).thenReturn(quote);
        when(massUtil.finalizeMediatorMultisig(fundRequest)).thenReturn(Mono.just(finalizeResponse));
        when(monero.controlWallet(WalletState.OPEN, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, "test"))
            .thenReturn(Mono.just(walletStateResponse));
        when(lightning.lookupInvoice(quote.get().getQuote_id()))
            .thenReturn(Mono.just(invoiceLookupResponse));
        when(monero.transfer(quote.get().getSwap_address(), quote.get().getAmount()))
            .thenReturn(Mono.just(transferResponse));
        
        Mono<FundResponse> testResponse = swapService.fundMoneroSwap(fundRequest);
        
        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r.getTxid()
          .equals(expectedTxId))
        .verifyComplete();
    }

    @Test
    @DisplayName("Cancel Monero Swap Test")
    public void cancelMoneroSwapTest() throws SSLException, IOException {
        String expectedAddress = "54mulitsigaddress";
        String txset = "expectedTxset";
        SwapRequest swapRequest = SwapRequest.builder()
            .hash("hash").preimage(new byte[32]).build();
        Optional <MoneroQuote> quote = Optional.of(MoneroQuote.builder()
            .amount(0.123).dest_address(expectedAddress)
            .funding_txid("0xfundtxid")
            .mediator_filename("mfn")
            .swap_filename("sfn")
            .mediator_finalize_msig("mfmsig")
            .quote_id("lnbcrtquoteid")
            .swap_address(expectedAddress)
            .swap_finalize_msig("sfmisg").build());
        SweepAllResult result = SweepAllResult.builder()
            .multisig_txset(txset)
            .build();
        SweepAllResponse sweepAllResponse = SweepAllResponse.builder()
            .result(result)
            .build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        List<String> txHashList = Lists.newArrayList();
        txHashList.add("tx123");
        SignResult signResult = SignResult.builder()
            .tx_data_hex("tx_data_hex").tx_hash_list(txHashList).build();
        SignResponse signResponse = SignResponse.builder()
            .result(signResult).build();
        SubmitResult submitResult = SubmitResult.builder()
            .tx_hash_list(txHashList).build();
        SubmitResponse submitResponse = SubmitResponse.builder()
            .result(submitResult).build();
        BalanceResult balanceResult = BalanceResult.builder()
            .blocks_to_unlock(0).build();
        BalanceResponse balanceResponse = BalanceResponse.builder()
            .result(balanceResult).build();
        // mocks
        when(quoteRepository.findById(anyString())).thenReturn(quote);
        when(monero.getBalance()).thenReturn(Mono.just(balanceResponse));
        when(monero.controlWallet(WalletState.OPEN, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.OPEN, quote.get().getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.get().getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.sweepAll("54testrpaddress")).thenReturn(Mono.just(sweepAllResponse));
        String mfn = quote.get().getMediator_filename();
        when(monero.controlWallet(WalletState.OPEN, mfn)).thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, mfn)).thenReturn(Mono.just(walletStateResponse));
        when(monero.signMultisig(anyString())).thenReturn(Mono.just(signResponse));
        when(monero.submitMultisig(anyString())).thenReturn(Mono.just(submitResponse));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(swapRequest,quote.get(), false)).thenReturn(Mono.just(entity));

        Mono<SwapResponse> testResponse = swapService.processCancel(swapRequest);
        
        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r
          .equals(SwapResponse.builder().build()))
        .verifyComplete();
    }

    @Test
    @DisplayName("Test Import / Export Info")
    public void importExportTest() {
        String expectedHash = "hash123";
        Optional <MoneroQuote> quote = Optional.of(MoneroQuote.builder()
            .amount(0.123).dest_address("address")
            .funding_txid("0xfundtxid")
            .mediator_filename("mfn")
            .swap_filename("sfn")
            .mediator_finalize_msig("mfmsig")
            .quote_id("lnbcrtquoteid")
            .swap_address("address")
            .swap_finalize_msig("sfmisg").build());
        InitRequest initRequest = InitRequest.builder()
            .hash("hash").importInfo("importInfo").build();
        InitResponse initResponse = InitResponse.builder().hash(expectedHash)
            .swapExportInfo("swapExportInfo").build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        BalanceResult balanceResult = BalanceResult.builder().blocks_to_unlock(0).build();
        BalanceResponse balanceResponse = BalanceResponse.builder().result(balanceResult).build();
            // mocks
        when(quoteRepository.findById(anyString())).thenReturn(quote);
        when(monero.controlWallet(WalletState.OPEN, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(massUtil.exportSwapInfo(quote.get(), initRequest)).thenReturn(Mono.just(initResponse));
        when(monero.getBalance()).thenReturn(Mono.just(balanceResponse));
        Mono<InitResponse> testResponse = swapService.importAndExportInfo(initRequest);

        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r.getHash()
          .equals(expectedHash))
        .verifyComplete();
    }

}
