package org.hiahatf.mass.services.bitcoin;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.SwapRequest;
import org.hiahatf.mass.models.bitcoin.SwapResponse;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling all Bitcoin swap logic
 */
@Service(Constants.BTC_SWAP_SERVICE)
public class SwapService {

    // TODO: Verify Transaction amount and address
    
    // TODO: Attempt to relay the tx metadata

    // TODO: Attempt to pay invoice

    // TODO: Auotmated refund logic on invoice payment failure

    // TODO: Return response
    
    public Mono<SwapResponse> processBitcoinSwap(SwapRequest swapRequest) {
        SwapResponse response = SwapResponse.builder().build();
        return Mono.just(response);
    }

    

}
