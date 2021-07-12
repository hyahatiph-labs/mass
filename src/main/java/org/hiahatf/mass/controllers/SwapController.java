package org.hiahatf.mass.controllers;

import org.hiahatf.mass.models.SwapRequest;
import org.hiahatf.mass.models.SwapResponse;
import org.hiahatf.mass.services.SwapService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

/**
 * Controller for handling swaps
 */
@RestController("SwapController")
@RequestMapping
public class SwapController extends BaseController {

    private SwapService swapService;

    /**
     * Swap Controller constructor dependency injection
     * @param quoteService
     */
    @Autowired
    public SwapController(SwapService service) {
        this.swapService = service;
    }

    /**
     * This endpoint reaches utilizes lightining network
     * hold invoices to verify in-flight payments and settles
     * with the equivalent amount in Monero.
     * @return SwapResponse
     */
    @PostMapping("/swap/xmr")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<SwapResponse> fetchMoneroQuote(@RequestBody SwapRequest request) {
        return swapService.processMoneroSwap(request);
    }

}
