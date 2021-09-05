package org.hiahatf.mass.controllers.monero;

import org.hiahatf.mass.controllers.BaseController;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.InitResponse;
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
     * The /swap/fund endpoint only commits funds to the consensus wallet.
     * The associated invoice must be in ACCEPTED status first.
     * triggered.
     * @param request
     * @return Mono<FundResponse>
     */
    @PostMapping(Constants.XMR_SWAP_FUND_PATH)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<FundResponse> fundMoneroSwap(@RequestBody FundRequest request) {
        return swapService.fundMoneroSwap(request);
    }

    /**
     * The /swap/initialize endpoint is used to export
     * multisig info. And finish configuring swap and mediator for signing
     * and submitting from the consensus wallet
     * @param request
     * @return Mono<InitResponse>
     */
    @PostMapping(Constants.XMR_SWAP_INIT_PATH)
    @ResponseStatus(HttpStatus.OK)
    public Mono<InitResponse> initializeMoneroSwap(@RequestBody InitRequest request) {
        return swapService.importAndExportInfo(request);
    }

    /**
     * The /swap/cancel/xmr endpoint is used to import
     * multisig info. There is a ~1-2HR min window of opportunity
     * that the client can choose to back out of the swap. Beyond the 
     * consensus wallet finality HTLC funds are consumed and client will
     * forfeit the funds
     * @param request
     * @return Mono<FundResponse>
     */
    @PostMapping(Constants.XMR_CANCEL_PATH) 
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<SwapResponse> cancelMoneroSwap(@RequestBody SwapRequest request) {
        return swapService.processCancel(request);
    }

    /**
     * This endpoint reaches utilizes lightning network
     * hold invoices to verify in-flight payments and settles
     * with the equivalent amount in Monero. 
     * @return Mono<SwapResponse>
     */
    @PostMapping(Constants.XMR_SWAP_FINAL_PATH) 
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<SwapResponse> finalizeMoneroSwap(@RequestBody SwapRequest request) {
        return swapService.transferMonero(request);
    }

}
