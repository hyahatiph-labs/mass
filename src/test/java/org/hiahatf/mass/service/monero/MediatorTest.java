package org.hiahatf.mass.service.monero;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.models.monero.multisig.SignResponse;
import org.hiahatf.mass.models.monero.multisig.SignResult;
import org.hiahatf.mass.models.monero.multisig.SubmitResponse;
import org.hiahatf.mass.models.monero.multisig.SubmitResult;
import org.hiahatf.mass.models.monero.multisig.SweepAllResponse;
import org.hiahatf.mass.models.monero.multisig.SweepAllResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResult;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.monero.Mediator;
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

/**
 * Tests for Mediator
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class MediatorTest {
    
    @Mock
    Monero monero;
    @Mock
    Lightning lightning;
    @Mock
    MassUtil massUtil;
    @Mock
    ResponseEntity<Void> entity;
    @Mock
    MoneroQuoteRepository moneroQuoteRepository;
    @InjectMocks
    Mediator mediator = new Mediator(moneroQuoteRepository, "quoteId", lightning, monero, 
        massUtil, "refundAddress");
    
    @Test
    @DisplayName("Mediator Test")
    public void mediatorTest() throws SSLException, IOException {
        String txset = "txset";
        XmrQuoteTable table = XmrQuoteTable.builder()
            .amount(0.1)
            .dest_address("dest_address")
            .funding_txid("funding_txid")
            .mediator_filename("mediator_filename")
            .swap_address("swap_address")
            .swap_filename("swap_filename")
            .payment_hash(new byte[32])
            .quote_id("quoteId")
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
        SweepAllResult result = SweepAllResult.builder()
            .multisig_txset(txset)
            .build();
        SweepAllResponse sweepAllResponse = SweepAllResponse.builder()
            .result(result)
            .build();
        InitResponse initResponse = InitResponse.builder().build();
        // mocks
        when(moneroQuoteRepository.findById(anyString())).thenReturn(Optional.of(table));
        when(entity.getStatusCode()).thenReturn(HttpStatus.OK);
        when(lightning.handleInvoice(table, true)).thenReturn(Mono.just(entity));
        when(monero.controlWallet(WalletState.OPEN, table.getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, table.getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.OPEN, table.getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, table.getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(massUtil.exportSwapInfo(any(), any())).thenReturn(Mono.just(initResponse));
        when(monero.signMultisig(anyString())).thenReturn(Mono.just(signResponse));
        when(monero.submitMultisig(anyString())).thenReturn(Mono.just(submitResponse));
        when(monero.sweepAll(anyString())).thenReturn(Mono.just(sweepAllResponse));

        mediator.run();

        verify(monero, atLeastOnce()).sweepAll(anyString());
    }

}
