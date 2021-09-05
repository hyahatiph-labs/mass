package org.hiahatf.mass.controllers.bitcoin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.hiahatf.mass.models.bitcoin.InitRequest;
import org.hiahatf.mass.models.bitcoin.SwapRequest;
import org.hiahatf.mass.models.bitcoin.SwapResponse;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.services.bitcoin.SwapService;
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
    SwapService service;

    @InjectMocks
    SwapController controller = new SwapController(service);

    @Test
    @DisplayName("Swap Controller Fund Test")
    public void fundBitconSwapTest() {
        String hash = "hash";
        String txid = "txid";
        FundRequest request = FundRequest.builder().hash(hash).build();
        FundResponse swap = FundResponse.builder().txid(txid).build();
        when(service.fundBitcoinSwap(request)).thenReturn(Mono.just(swap));
        Mono<FundResponse> testFund = controller.fundBitcoinSwap(request);
        assertEquals(swap.getTxid(), testFund.block().getTxid());
    }

    @Test
    @DisplayName("Swap Controller Initialize Test")
    public void initBitcoinSwapTest() {
        String hash = "hash";
        String info = "info";
        InitRequest request = InitRequest.builder().hash(hash).build();
        InitResponse init = InitResponse.builder().swapExportInfo(info).build();
        when(service.importAndExportInfo(request)).thenReturn(Mono.just(init));
        Mono<InitResponse> testFund = controller.initializeBitcoinSwap(request);
        assertEquals(init.getSwapExportInfo(), testFund.block().getSwapExportInfo());
    }

    @Test
    @DisplayName("Swap Controller Finalize Test")
    public void finalizeBitcoinSwapTest() {
        String hash = "hash";
        String preimage = "preimage";
        SwapRequest request = SwapRequest.builder().hash(hash).build();
        SwapResponse swap = SwapResponse.builder().preimage(preimage).build();
        when(service.processBitcoinSwap(request)).thenReturn(Mono.just(swap));
        Mono<SwapResponse> testFund = controller.finalizeBitcoinSwap(request);
        assertEquals(swap.getPreimage(), testFund.block().getPreimage());
    }

}
