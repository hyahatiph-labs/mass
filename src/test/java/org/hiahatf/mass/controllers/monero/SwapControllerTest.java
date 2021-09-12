package org.hiahatf.mass.controllers.monero;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.InitResponse;
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
    @DisplayName("Swap Controller Fund Test")
    public void fundMoneroSwapTest() {
        String hash = "hash";
        String txid = "txid";
        FundRequest request = FundRequest.builder().hash(hash).build();
        FundResponse swap = FundResponse.builder().txid(txid).build();
        when(swapService.fundMoneroSwap(request)).thenReturn(Mono.just(swap));
        Mono<FundResponse> testFund = controller.fundMoneroSwap(request);
        assertEquals(swap.getTxid(), testFund.block().getTxid());
    }

    @Test
    @DisplayName("Swap Controller Initialize Test")
    public void initializeMoneroSwapTest() {
        String hash = "hash";
        String info = "info";
        InitRequest request = InitRequest.builder().hash(hash).build();
        InitResponse init = InitResponse.builder().swapExportInfo(info).build();
        when(swapService.importAndExportInfo(request)).thenReturn(Mono.just(init));
        Mono<InitResponse> testInit = controller.initializeMoneroSwap(request);
        assertEquals(init.getSwapExportInfo(), testInit.block().getSwapExportInfo());
    }

    @Test
    @DisplayName("Swap Controller Cancel Test")
    public void cancelMoneroSwapTest() {
        String hash = "hash";
        SwapRequest request = SwapRequest.builder().hash(hash).build();
        SwapResponse swap = SwapResponse.builder().build();
        when(swapService.processCancel(request)).thenReturn(Mono.just(swap));
        Mono<SwapResponse> testCancel = controller.cancelMoneroSwap(request);
        assertEquals(swap, testCancel.block());
    }

    @Test
    @DisplayName("Swap Controller Finalize Test")
    public void fetchMoneroQuoteTest() {
        String hash = "hash";
        SwapRequest request = SwapRequest.builder().hash(hash).build();
        SwapResponse swap = SwapResponse.builder().hash(hash).build();
        when(swapService.transferMonero(request)).thenReturn(Mono.just(swap));
        Mono<SwapResponse> testSwap = controller.finalizeMoneroSwap(request);
        assertEquals(swap.getHash(), testSwap.block().getHash());
    }
    
}
