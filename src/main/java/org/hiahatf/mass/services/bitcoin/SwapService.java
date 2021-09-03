package org.hiahatf.mass.services.bitcoin;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;

import javax.net.ssl.SSLException;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.bitcoin.BtcQuoteTable;
import org.hiahatf.mass.models.bitcoin.SwapRequest;
import org.hiahatf.mass.models.bitcoin.SwapResponse;
import org.hiahatf.mass.models.lightning.PaymentStatus;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.bitcoin.InitRequest;
import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.BitcoinQuoteRepository;
import org.hiahatf.mass.services.rate.RateService;
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
    public static boolean isWalletOpen;
    private String massWalletFilename;
    private RateService rateService;
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
        @Value(Constants.RP_ADDRESS) String rpAddress, RateService rateService) {
            this.quoteRepository = quoteRepository;
            this.massWalletFilename = massWalletFilename;
            this.rateService = rateService;
            this.lightning = lightning;
            this.rpAddress = rpAddress;
            this.massUtil = massUtil;
            this.monero = monero;
    }

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

    /**
     * Import info from the client and export the necessary info from the swap
     * wallet. This is dependent on completion of consensus wallet
     * funding. Blocks remaining on funding transaction must be == 0;
     * @param initRequest
     * @return Mono<InitResponse> - swap export_multisig_info 
     */
    public Mono<InitResponse> importAndExportInfo(InitRequest initRequest) {
        BtcQuoteTable table = quoteRepository.findById(initRequest.getHash()).get();
        String sfn = table.getSwap_filename();
        isWalletOpen = true;
        return monero.controlWallet(WalletState.OPEN, sfn).flatMap(o -> {
            return monero.getBalance().flatMap(b -> {
                int blocksRemaining = b.getResult().getBlocks_to_unlock();
                if(blocksRemaining != 0) {
                    String msg = MessageFormat.format(Constants.FUNDING_ERROR, 
                        String.valueOf(blocksRemaining));
                    return Mono.error(new MassException(msg));
                }
                return monero.controlWallet(WalletState.CLOSE, sfn).flatMap(c -> {
                    isWalletOpen = false;
                    return decodePayReq(initRequest, table, sfn);
                });
            });
        });
    }

    /**
     * Call Lightning API for decoding the payment request and perform validations.
     * Upon success attempt to place payment in-flight for htlc.
     * @param request
     * @param rate
     * @return Mono<Quote>
     */
    private Mono<InitResponse> decodePayReq(InitRequest request, BtcQuoteTable table, String sfn) {
        String rate = rateService.getMoneroRate();
        Double parsedRate = massUtil.parseMoneroRate(rate);
        try {
            logger.info("Decoding payment request");
            return lightning.decodePaymentRequest(request.getPaymentRequest()).flatMap(p -> {
                Double value = Double.valueOf(p.getNum_satoshis());
                // validate expiry is not set for a longer than limit
                if(Integer.valueOf(p.getExpiry()) > Constants.EXPIRY_LIMIT) {
                    return Mono.error(new MassException(Constants.EXPIRY_ERROR));
                }
                // calculate the amount of monero we expect
                Double rawAmt = value / (parsedRate * Constants.COIN);
                Double moneroAmt = BigDecimal.valueOf(rawAmt)
                    .setScale(12, RoundingMode.HALF_UP).doubleValue();
                if(moneroAmt < table.getAmount()) {
                    return Mono.error(new MassException(Constants.INVALID_AMT_ERROR));
                }
                return payInvoice(request, sfn);
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }
    
    private Mono<InitResponse> payInvoice(InitRequest initRequest, String sfn) {
        try {
            return lightning.sendPayment(initRequest.getPaymentRequest()).flatMap(pay -> {
                if(pay.getStatus() == PaymentStatus.FAILED) {
                    return Mono.error(new MassException(Constants.FATAL_SWAP_ERROR));
                }
                return massUtil.rExportSwapInfo(sfn, initRequest);
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

    public Mono<SwapResponse> processBitcoinSwap(SwapRequest swapRequest) {
        // TODO: run describe on txset
        // TODO: sweep and sign to address (use rpAddress for now)
        // TODO: return preimage
        SwapResponse response = SwapResponse.builder().build();
        return Mono.just(response);
    }

}
