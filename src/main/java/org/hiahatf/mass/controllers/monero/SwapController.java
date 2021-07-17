package org.hiahatf.mass.controllers.monero;

import org.hiahatf.mass.controllers.BaseController;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.SwapResponse;
import org.hiahatf.mass.services.monero.SwapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Controller for handling Monero swaps
 */
@RestController
@RequestMapping
public class SwapController extends BaseController {

    private SwapService swapService;

    /**
     * Swap Controller constructor dependency injection
     * @param service
     */
    @Autowired
    public SwapController(SwapService service) {
        this.swapService = service;
    }

    /**
     * This endpoint reaches utilizes lightning network
     * hold invoices to verify in-flight payments and settles
     * with the equivalent amount in Monero.
     * @return Mono<SwapResponse>
     */
    @PostMapping(Constants.XMR_SWAP_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SwapResponse> fetchMoneroSwap(@RequestBody SwapRequest request) {
        return swapService.processMoneroSwap(request);
    }

}
