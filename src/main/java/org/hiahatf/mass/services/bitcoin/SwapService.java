package org.hiahatf.mass.services.bitcoin;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.BtcQuoteTable;
import org.hiahatf.mass.models.bitcoin.SwapRequest;
import org.hiahatf.mass.models.bitcoin.SwapResponse;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.repo.BitcoinQuoteRepository;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.hiahatf.mass.util.MassUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for handling all Bitcoin swap logic
 */
@Service(Constants.BTC_SWAP_SERVICE)
public class SwapService {

    private Logger logger = LoggerFactory.getLogger(SwapService.class);
    private BitcoinQuoteRepository quoteRepository;
    private String massWalletFilename;
    public static boolean isWalletOpen;
    private Lightning lightning;
    private MassUtil massUtil;
    private String rpAddress;
    private Monero monero;

    /**
     * Swap service dependency injection
     */
    @Autowired
    public SwapService(
        BitcoinQuoteRepository quoteRepository, Lightning lightning, Monero monero,
        MassUtil massUtil, @Value(Constants.MASS_WALLET_FILENAME) String massWalletFilename,
        @Value(Constants.RP_ADDRESS) String rpAddress) {
            this.quoteRepository = quoteRepository;
            this.massWalletFilename = massWalletFilename;
            this.lightning = lightning;
            this.rpAddress = rpAddress;
            this.massUtil = massUtil;
            this.monero = monero;
    }

    // TODO: verify pending balance to multisig wallet
    
    // TODO: Attempt to relay the multisig_txset

    // TODO: Attempt to pay invoice

    // TODO: Auotmated refund logic on invoice payment failure

    /**
     * Finalize multisig and return swap address for verification and funding
     * by the client.
     * @param request
     * @return
     */
    public Mono<FundResponse> fundBitcoinSwap(FundRequest request) {
        if(isWalletOpen) {
            return Mono.error(new MassException(Constants.WALLET_ERROR));
        }
        BtcQuoteTable table = quoteRepository.findById(request.getHash()).get();
        return massUtil.rFinalizeSwapMultisig(request, table.getSwap_filename()).flatMap(fm -> {
            String address = fm.getResult().getAddress();
            table.setSwap_address(address);
            quoteRepository.save(table);
            FundResponse fundResponse = FundResponse.builder().swapAddress(address).build();
            return Mono.just(fundResponse);
        });
    }

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
