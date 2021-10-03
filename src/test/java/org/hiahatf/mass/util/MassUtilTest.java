package org.hiahatf.mass.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.lightning.Amount;
import org.hiahatf.mass.models.lightning.Liquidity;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.MoneroQuote;
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
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletResponse;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletResult;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResult;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
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

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Test class for the utility methods
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class MassUtilTest {

    @Mock
    Lightning lightning;

    @Mock
    Monero monero;

    @Mock
    MoneroQuoteRepository moneroQuoteRepository;

    @InjectMocks
    private MassUtil util = 
        new MassUtil(0.01, 10000, 10000000, lightning, monero, moneroQuoteRepository);
    
    @Test
    @DisplayName("Parse Rate Test")
    public void parseRateTest() {
        String data = "{BTC: 0.0076543}";
        Double testRate = util.parseMoneroRate(data);
        Double expectedRate = 0.007730843;
        assertEquals(expectedRate, testRate);
    }

    @Test
    @DisplayName("Validate Liquidity Test")
    public void validateLiquidityTest() throws SSLException, IOException {
        Amount amount = Amount.builder()
           .sat("1000000").msat("100000000").build();
        Liquidity liquidity = Liquidity.builder()
            .local_balance(amount)
            .remote_balance(amount).build();
        when(lightning.fetchBalance()).thenReturn(Mono.just(liquidity));

        Mono<Boolean> testValue = util.validateLiquidity(20000.0, LiquidityType.INBOUND);

        StepVerifier.create(testValue)
        .expectNextMatches(b -> b
          .equals(true))
        .verifyComplete();
    }

    @Test
    @DisplayName("Configure Multisig Test")
    public void configureMultisigTest() {
        String testInfo = "testInfo";
        String testHash = "testHash";
        String testMultisigInfo = "multisigInfo";
        List<String> infoList = Lists.newArrayList();
        infoList.add(testMultisigInfo);
        // mocks
        CreateWalletResult createWalletResult = CreateWalletResult.builder().build();
        CreateWalletResponse createWalletResponse = CreateWalletResponse.builder()
            .result(createWalletResult).build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
        .result(walletStateResult).build();
        PrepareResult prepareResult = PrepareResult.builder()
            .multisig_info(testMultisigInfo).build();
        PrepareResponse prepareResponse = PrepareResponse.builder().result(prepareResult).build();
        MakeResult makeResult = MakeResult.builder().multisig_info(testMultisigInfo).build();
        MakeResponse makeResponse = MakeResponse.builder().result(makeResult).build();
        when(monero.createWallet(anyString())).thenReturn(Mono.just(createWalletResponse));
        when(monero.controlWallet(any(), anyString())).thenReturn(Mono.just(walletStateResponse));
        when(monero.prepareMultisig()).thenReturn(Mono.just(prepareResponse));
        when(monero.makeMultisig(anyList())).thenReturn(Mono.just(makeResponse));

        Mono<MultisigData> testData = util.configureMultisig(testInfo, testHash);

        StepVerifier.create(testData)
        .expectNextMatches(d -> d.getClientMultisigInfo()
          .equals(testInfo))
        .verifyComplete();
    }

    @Test
    @DisplayName("Export Multisig Test")
    public void exportMultisigTest() {
        String importInfo = "MultisigIinfo";
        MoneroQuote quote = MoneroQuote.builder()
            .amount(0.123).dest_address("54destx")
            .funding_txid("0xfundtxid")
            .mediator_filename("mfn").mediator_finalize_msig("mfmsig")
            .quote_id("lnbcrtquoteid")
            .swap_address("54swapx").swap_filename("sfn")
            .swap_finalize_msig("sfmisg").build();
        InitRequest initRequest = InitRequest.builder()
            .hash("hash").importInfo("importInfo").build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        ExportInfoResult exportInfoResult = ExportInfoResult.builder()
            .info(importInfo).build();
        ExportInfoResponse exportInfoResponse = ExportInfoResponse.builder()
            .result(exportInfoResult).build();
        ImportInfoResult importInfoResult = ImportInfoResult.builder()
            .n_outputs(1).build();
        ImportInfoResponse importInfoResponse = ImportInfoResponse.builder()
            .result(importInfoResult).build();
        // mocks
        when(monero.controlWallet(WalletState.OPEN, quote.getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.OPEN, quote.getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.exportMultisigInfo()).thenReturn(Mono.just(exportInfoResponse));
        when(monero.importMultisigInfo(anyList())).thenReturn(Mono.just(importInfoResponse));

        Mono<InitResponse> testData = util.exportSwapInfo(quote, initRequest);

        StepVerifier.create(testData)
        .expectNextMatches(d -> d.getSwapExportInfo()
          .equals(importInfo))
        .verifyComplete();
    }

    @Test
    @DisplayName("Finalize Multisig Test")
    public void finalizeMultisigTest() {
        String expectedAddress = "addy123";
        Optional <MoneroQuote> quote = Optional.of(MoneroQuote.builder()
            .amount(0.123).dest_address("54destx")
            .funding_txid("0xfundtxid")
            .mediator_filename("mfn").mediator_finalize_msig("mfmsig")
            .quote_id("lnbcrtquoteid")
            .swap_address("54swapx").swap_filename("sfn")
            .swap_finalize_msig("sfmisg").build());
        FundRequest fundRequest = FundRequest.builder()
            .hash("hash").makeMultisigInfo("makeMultisigInfo").build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        FinalizeResult finalizeResult = FinalizeResult.builder().address(expectedAddress).build();
        FinalizeResponse finalizeResponse = FinalizeResponse.builder().result(finalizeResult).build();
        // mocks
        when(moneroQuoteRepository.findById(anyString())).thenReturn(quote);
        when(monero.controlWallet(WalletState.OPEN, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.get().getSwap_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.OPEN, quote.get().getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, quote.get().getMediator_filename()))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.finalizeMultisig(anyList())).thenReturn(Mono.just(finalizeResponse));

        Mono<FinalizeResponse> testResponse = util.finalizeMediatorMultisig(fundRequest);

        StepVerifier.create(testResponse)
        .expectNextMatches(t-> t.getResult().getAddress()
          .equals(expectedAddress))
        .verifyComplete();
    }

    @Test
    @DisplayName("Reverse-logic Configure Multisig Test")
    public void rConfigureMultisigTest() {
        String testHash = "testHash";
        String testMultisigInfo = "multisigInfo";
        List<String> infoList = Lists.newArrayList();
        infoList.add(testMultisigInfo);
        // mocks
        CreateWalletResult createWalletResult = CreateWalletResult.builder().build();
        CreateWalletResponse createWalletResponse = CreateWalletResponse.builder()
            .result(createWalletResult).build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
        .result(walletStateResult).build();
        PrepareResult prepareResult = PrepareResult.builder()
            .multisig_info(testMultisigInfo).build();
        PrepareResponse prepareResponse = PrepareResponse.builder().result(prepareResult).build();
        MakeResult makeResult = MakeResult.builder().multisig_info(testMultisigInfo).build();
        MakeResponse makeResponse = MakeResponse.builder().result(makeResult).build();
        when(monero.createWallet(anyString())).thenReturn(Mono.just(createWalletResponse));
        when(monero.controlWallet(any(), anyString())).thenReturn(Mono.just(walletStateResponse));
        when(monero.prepareMultisig()).thenReturn(Mono.just(prepareResponse));
        when(monero.makeMultisig(anyList())).thenReturn(Mono.just(makeResponse));

        Mono<MultisigData> testData = util.rConfigureMultisig(infoList, testHash);

        StepVerifier.create(testData)
        .expectNextMatches(d -> d.getClientMultisigInfos()
          .equals(infoList))
        .verifyComplete();
    }

    @Test
    @DisplayName("Reverse-logic Export Multisig Test")
    public void rExportMultisigTest() {
        String importInfo = "MultisigIinfo";
        List<String> infos = Lists.newArrayList();
        infos.add("info1");
        infos.add("info2");
        org.hiahatf.mass.models.bitcoin.InitRequest initRequest = 
            org.hiahatf.mass.models.bitcoin.InitRequest.builder()
            .hash("hash").importInfos(infos).build();
        WalletStateResult walletStateResult = WalletStateResult.builder().build();
        WalletStateResponse walletStateResponse = WalletStateResponse.builder()
            .result(walletStateResult).build();
        ExportInfoResult exportInfoResult = ExportInfoResult.builder()
            .info(importInfo).build();
        ExportInfoResponse exportInfoResponse = ExportInfoResponse.builder()
            .result(exportInfoResult).build();
        ImportInfoResult importInfoResult = ImportInfoResult.builder()
            .n_outputs(1).build();
        ImportInfoResponse importInfoResponse = ImportInfoResponse.builder()
            .result(importInfoResult).build();
        // mocks
        when(monero.controlWallet(WalletState.OPEN, "filename"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.controlWallet(WalletState.CLOSE, "filename"))
            .thenReturn(Mono.just(walletStateResponse));
        when(monero.exportMultisigInfo()).thenReturn(Mono.just(exportInfoResponse));
        when(monero.importMultisigInfo(anyList())).thenReturn(Mono.just(importInfoResponse));

        Mono<InitResponse> testData = util.rExportSwapInfo("filename", initRequest);

        StepVerifier.create(testData)
        .expectNextMatches(d -> d.getSwapExportInfo()
          .equals(importInfo))
        .verifyComplete();
    }

}