package org.hiahatf.mass.service.bitcoin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.apache.commons.codec.binary.Hex;
import org.hiahatf.mass.models.bitcoin.BtcQuoteTable;
import org.hiahatf.mass.models.bitcoin.InitRequest;
import org.hiahatf.mass.models.bitcoin.SwapRequest;
import org.hiahatf.mass.models.bitcoin.SwapResponse;
import org.hiahatf.mass.models.lightning.PaymentRequest;
import org.hiahatf.mass.models.lightning.PaymentStatus;
import org.hiahatf.mass.models.lightning.RouterSendResponse;
import org.hiahatf.mass.models.monero.Description;
import org.hiahatf.mass.models.monero.Destination;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.models.monero.balance.BalanceResponse;
import org.hiahatf.mass.models.monero.balance.BalanceResult;
import org.hiahatf.mass.models.monero.multisig.DescribeResponse;
import org.hiahatf.mass.models.monero.multisig.DescribeResult;
import org.hiahatf.mass.models.monero.multisig.FinalizeResponse;
import org.hiahatf.mass.models.monero.multisig.FinalizeResult;
import org.hiahatf.mass.models.monero.multisig.SignResponse;
import org.hiahatf.mass.models.monero.multisig.SignResult;
import org.hiahatf.mass.models.monero.multisig.SubmitResponse;
import org.hiahatf.mass.models.monero.multisig.SubmitResult;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResult;
import org.hiahatf.mass.repo.BitcoinQuoteRepository;
import org.hiahatf.mass.services.bitcoin.SwapService;
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
 * Tests for Bitcoin Swap Service
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class BitcoinSwapServiceTest {
    
    @Mock
    private BitcoinQuoteRepository quoteRepository;
    @Mock
    private RateService rateService;
    @Mock
    private Lightning lightning;
    @Mock
    private MassUtil massUtil;
    @Mock
    private Monero monero;
    @InjectMocks
    SwapService swapService = new SwapService(quoteRepository, lightning, monero, massUtil, 
        rateService, "sendAddy");

    @Test
    @DisplayName("Fund Swap Service Test")
    public void fundSwapTest() {
        String expectedAddress = "addy123";
        Optional<BtcQuoteTable> table = Optional.of(BtcQuoteTable.builder()
            .amount(0.1).funding_txid("funding_txid").payment_hash(new byte[32])
            .preimage(new byte[32]).quote_id("quote_id").refund_address("refund_address")
            .swap_filename("swap_filename")
            .swap_address("swap_address").build());
        FundRequest fundRequest = FundRequest.builder().hash("hash")
            .makeMultisigInfo("makeMultisigInfo").build();
        FinalizeResult finalizeResult = FinalizeResult.builder()
            .address(expectedAddress).build();
        FinalizeResponse finalizeResponse = FinalizeResponse.builder()
            .result(finalizeResult).build();
        // mocks
        when(quoteRepository.findById(anyString())).thenReturn(table);
        when(massUtil.rFinalizeSwapMultisig(fundRequest, table.get().getSwap_filename()))
            .thenReturn(Mono.just(finalizeResponse));

        Mono<FundResponse> testResponse = swapService.fundBitcoinSwap(fundRequest);

        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r.getSwapAddress()
          .equals(expectedAddress))
        .verifyComplete();
    }

    @Test
    @DisplayName("Initialize Swap Service Test")
    public void initializeSwapTest() throws SSLException, IOException {
        String expectedSwapInfo = "Multisig123";
        Optional<BtcQuoteTable> table = Optional.of(BtcQuoteTable.builder()
            .amount(0.1).funding_txid("funding_txid").payment_hash(new byte[32])
            .preimage(new byte[32]).quote_id("quote_id").refund_address("refund_address")
            .swap_filename("swap_filename")
            .swap_address("swap_address").build());
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        BalanceResult balanceResult = BalanceResult.builder().blocks_to_unlock(0).build();
        BalanceResponse balanceResponse = BalanceResponse.builder().result(balanceResult).build();
        InitRequest initRequest = InitRequest.builder().hash("hash")
            .importInfo("importInfo").paymentRequest("lntest123").build();
        InitResponse initResponse = InitResponse.builder().hash("hash")
            .swapExportInfo(expectedSwapInfo).build();
        PaymentRequest paymentRequest = PaymentRequest.builder()
            .expiry("7200").num_satoshis("80000").build();
        RouterSendResponse routerSendResponse = RouterSendResponse.builder()
            .status(PaymentStatus.IN_FLIGHT).build();
        // mocks
        when(quoteRepository.findById(anyString())).thenReturn(table);
        when(monero.controlWallet(WalletState.OPEN, table.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, table.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.getBalance()).thenReturn(Mono.just(balanceResponse));
        when(rateService.getMoneroRate()).thenReturn("{BTC: 0.00777}");
        when(massUtil.parseMoneroRate(anyString())).thenReturn(0.008);
        when(lightning.decodePaymentRequest(anyString())).thenReturn(Mono.just(paymentRequest));
        when(lightning.sendPayment(anyString())).thenReturn(Mono.just(routerSendResponse));
        when(massUtil.rExportSwapInfo(table.get().getSwap_filename(), initRequest))
            .thenReturn(Mono.just(initResponse));

        Mono<InitResponse> testResponse = swapService.importAndExportInfo(initRequest);

        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r.getSwapExportInfo()
          .equals(expectedSwapInfo))
        .verifyComplete();
    }

    @Test
    @DisplayName("Finalize Swap Service Test")
    public void finalizeSwapTest() {
        String expectedAddress = "sendAddy";
        Optional<BtcQuoteTable> table = Optional.of(BtcQuoteTable.builder()
            .amount(0.1).funding_txid("funding_txid").payment_hash(new byte[32])
            .preimage(new byte[32]).quote_id("quote_id").refund_address("refund_address")
            .swap_filename("swap_filename")
            .swap_address("swap_address").build());
        List<Destination> destinations = Lists.newArrayList();
        Destination destination = Destination.builder().address(expectedAddress)
            .amount(1000000000000L).build();
        destinations.add(destination);
        Description desc = Description.builder().recipients(destinations).build();
        DescribeResult describeResult = DescribeResult.builder().desc(desc).build();
        DescribeResponse describeResponse = DescribeResponse.builder().result(describeResult).build();
        SignResult signResult = SignResult.builder().tx_data_hex("tx_data_hex").build();
        SignResponse signResponse = SignResponse.builder().result(signResult).build();
        List<String> txHashList = Lists.newArrayList();
        SubmitResult submitResult = SubmitResult.builder().tx_hash_list(txHashList).build();
        SubmitResponse submitResponse = SubmitResponse.builder().result(submitResult).build();
        SwapRequest swapRequest = SwapRequest.builder().hash("hash").txset("").build();
        // mocks
        when(quoteRepository.findById(anyString())).thenReturn(table);
        when(monero.describeTransfer(anyString())).thenReturn(Mono.just(describeResponse));
        when(monero.signMultisig(anyString())).thenReturn(Mono.just(signResponse));
        when(monero.submitMultisig(anyString())).thenReturn(Mono.just(submitResponse));

        Mono<SwapResponse> testResponse = swapService.processBitcoinSwap(swapRequest);

        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r.getPreimage()
          .equals(Hex.encodeHexString(table.get().getPreimage())))
        .verifyComplete();
    }

}
