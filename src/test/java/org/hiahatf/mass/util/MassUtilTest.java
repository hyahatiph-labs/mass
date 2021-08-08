package org.hiahatf.mass.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.lightning.Amount;
import org.hiahatf.mass.models.lightning.Liquidity;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.multisig.MakeResponse;
import org.hiahatf.mass.models.monero.multisig.MakeResult;
import org.hiahatf.mass.models.monero.multisig.PrepareResponse;
import org.hiahatf.mass.models.monero.multisig.PrepareResult;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletResponse;
import org.hiahatf.mass.models.monero.wallet.create.CreateWalletResult;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResponse;
import org.hiahatf.mass.models.monero.wallet.state.WalletStateResult;
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

    @InjectMocks
    private MassUtil util = 
        new MassUtil(0.01, 10000, 10000000, lightning, monero);
    
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

}
