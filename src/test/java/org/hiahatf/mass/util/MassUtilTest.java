package org.hiahatf.mass.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.lightning.Amount;
import org.hiahatf.mass.models.lightning.Liquidity;
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

}
