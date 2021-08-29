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


    // TODO: move this to swap, will decode payment request before handling
    // handling the txset and revealing the preimage

    // /**
    //  * Call Lightning API for decoding the payment request
    //  * @param request
    //  * @param rate
    //  * @return Mono<Quote>
    //  */
    // private Mono<Quote> decodePayReq(Request request, Double rate) {
    //     try {
    //         return lightning.decodePaymentRequest(request.getPaymentRequest()).flatMap(p -> {
    //             Double value = Double.valueOf(p.getNum_satoshis());
    //             // validate expiry is not set for a longer than limit
    //             if(Integer.valueOf(p.getExpiry()) > Constants.EXPIRY_LIMIT) {
    //                 return Mono.error(new MassException(Constants.EXPIRY_ERROR));
    //             }
    //             // calculate the amount of monero we expect
    //             Double rawAmt = value / (rate * Constants.COIN);
    //             Double moneroAmt = BigDecimal.valueOf(rawAmt)
    //                 .setScale(12, RoundingMode.HALF_UP)
    //                 .doubleValue();
    //             return finalizeQuote(value, request, p, rate, moneroAmt);
    //         });
    //     } catch (SSLException se) {
    //         return Mono.error(new MassException(se.getMessage()));
    //     } catch (IOException ie) {
    //         return Mono.error(new MassException(ie.getMessage()));
    //     }
    // }
    

}
