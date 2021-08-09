package org.hiahatf.mass.controllers.monero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.SwapResponse;
import org.hiahatf.mass.services.monero.SwapService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class SwapControllerTest {
    
    @Mock
    SwapService swapService;

    @InjectMocks
    SwapController controller = new SwapController(swapService);

    @Test
    @DisplayName("Swap Controller Test")
    public void fetchMoneroQuoteTest() {
        String hash = "hash";
        SwapRequest request = SwapRequest.builder().hash(hash).build();
        SwapResponse swap = SwapResponse.builder().hash(hash).build();
        when(swapService.processMoneroSwap(request)).thenReturn(Mono.just(swap));
        Mono<SwapResponse> testSwap = controller.finalizeMoneroSwap(request);
        assertEquals(swap.getHash(), testSwap.block().getHash());
    }
    
}
