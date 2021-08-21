package org.hiahatf.mass.util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.monero.SwapRequest;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.FundResponse;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.XmrQuoteTable;
import org.hiahatf.mass.models.monero.multisig.FinalizeResponse;
import org.hiahatf.mass.models.monero.multisig.ImportInfoResponse;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.rpc.Lightning;
import org.hiahatf.mass.services.rpc.Monero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

@Service
public class MassUtil {

    private Logger logger = LoggerFactory.getLogger(MassUtil.class);
    private MoneroQuoteRepository moneroQuoteRepository;
    private Double markup;
    private Lightning lightning;
    private Monero monero;
    private Long minPay;
    private Long maxPay;

    /**
     * Mass utility class constructor
     * @param markup
     */
    public MassUtil(
        @Value(Constants.MARKUP) Double markup,@Value(Constants.MIN_PAY) long minPay,
        @Value(Constants.MAX_PAY) long maxPay,Lightning lightning, Monero monero,
        MoneroQuoteRepository moneroQuoteRepository) {
        this.markup = markup;
        this.minPay = minPay;
        this.maxPay = maxPay;
        this.lightning = lightning;
        this.monero = monero;
        this.moneroQuoteRepository = moneroQuoteRepository;
    }

    /**
     * Helper method for parsing Monero rate
     * @param rateString
     * @return Monero rate
     */
    public Double parseMoneroRate(String rateString) {
        Double parsedRate = Double
            .valueOf(rateString
            .split(Constants.SEMI_COLON_DELIMITER)[1]
            .split(Constants.RIGHT_BRACKET_DELIMITER)[0]);
        Double realRate = (parsedRate * markup) + parsedRate;
        // create the real rate by adding the markup to parsed rate
        logger.info(Constants.PARSE_RATE_MSG, parsedRate, realRate);;
        return realRate;
    }

    /**
     * Perform validations on channel balance to ensure
     * that a payment proposed on the quote MAY
     * possibly be fulfilled.
     * @param value - satoshi value of invoice
     * @return Mono<Boolean>
     */
    public Mono<Boolean> validateLiquidity(Double value, LiquidityType type) {
        // payment threshold validation
        long lValue = value.longValue();
        boolean isValid = lValue <= maxPay && lValue >= minPay;
        if(!isValid) {
            String error = MessageFormat.format(
                Constants.PAYMENT_THRESHOLD_ERROR, 
                String.valueOf(minPay), String.valueOf(maxPay)
                );
            return Mono.error(new MassException(error));
        }
        try {
            return lightning.fetchBalance().flatMap(b -> {
                // sum of sats in channels remote balance
                long balance = type == LiquidityType.INBOUND ? 
                    Long.valueOf(b.getRemote_balance().getSat()) :
                    Long.valueOf(b.getLocal_balance().getSat());
                if(lValue <= balance) {
                    return Mono.just(true);
                }
                return Mono.error(new MassException(Constants.LIQUIDITY_ERROR));
            });
        } catch (SSLException se) {
            return Mono.error(new MassException(se.getMessage()));
        } catch (IOException ie) {
            return Mono.error(new MassException(ie.getMessage()));
        }
    }

    /**
     * This method configures 2/3 multisig using multisig info passed in the request.
     * 1) Create wallet and open wallet for mass swap.
     * 2) Make multisig with clients' prepare multisig info.
     * 3) Prepare multisig to share with mediator and client.
     * 4) Close swap wallet and perform steps 1,2 and 3 with mediator wallet.
     * 5) Pass prepare / make multisig info to final response.
     * 6) Close wallets and handle wallet control.
     * @param multisigInfo
     * @param hash
     * @return Mono<MultisigData>
     */
    public Mono<MultisigData> configureMultisig(String multisigInfo, String hash) {
        long unixTime = System.currentTimeMillis() / 1000L;
        String format = "{0}{1}";
        String swapFilename = MessageFormat.format(format, hash, String.valueOf(unixTime));
        String mediatorFilename = MessageFormat.format(format, swapFilename, "m");
        logger.info("Swap filename: {}", swapFilename);
        MultisigData data = MultisigData.builder()
            .swapFilename(swapFilename)
            .mediatorFilename(mediatorFilename)
            .clientMultisigInfo(multisigInfo)
            .build();
        logger.info("Creating swap wallet");
        return monero.createWallet(swapFilename).flatMap(sfn -> {
            return prepareSwapMultisig(data);
        });
    }

    /**
     * Helper method for preparing main swap wallet multisig
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> prepareSwapMultisig(MultisigData data) {
        String swapFilename = data.getSwapFilename();
        return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwo -> {
            logger.info("Opening swap wallet");
            return monero.prepareMultisig().flatMap(spm -> {
                logger.info("Preparing swap wallet");
                data.setSwapMakeMultisigInfo(spm.getResult().getMultisig_info());
                return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(scwc -> {
                    logger.info("Closing swap wallet");
                    return prepareMediatorMultisig(data);
                });
            });
        });
    }

    /**
     * Helper method for preparing mediator swap wallet multisig
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> prepareMediatorMultisig(MultisigData data) {
        logger.info("Creating mediator wallet");
        return monero.createWallet(data.getMediatorFilename()).flatMap(sfn -> {
            logger.info("Opening mediator wallet");
            return monero.controlWallet(WalletState.OPEN, 
            data.getMediatorFilename()).flatMap(mcwo -> {
                List<String> infoList = Lists.newArrayList();
                infoList.add(data.getClientMultisigInfo());
                infoList.add(data.getSwapMakeMultisigInfo());
                logger.info("Preparing mediator wallet");
                return monero.prepareMultisig().flatMap(mpm -> {
                    data.setMediatorMakeMultisigInfo(mpm.getResult().getMultisig_info());
                    return makeMediatorMultisig(data, infoList);
                });
            });
        });
    }

    /**
     * Helper method for making mediator swap wallet multisig
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> makeMediatorMultisig(MultisigData data, List<String> infoList) {
        logger.info("Making mediator multisig");
        return monero.makeMultisig(infoList).flatMap(mmm -> {
            data.setMediatorFinalizeMultisigInfo(mmm.getResult().getMultisig_info());
            logger.info("Closing mediator wallet");                        
            return monero.controlWallet(WalletState.CLOSE, 
                data.getMediatorFilename()).flatMap(mcwc -> {
                    return makeSwapMultisig(data);
            });        
        });
    }

    /**
     * Helper method for making main swap wallet multisig
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> makeSwapMultisig(MultisigData data) {
        String swapFilename = data.getSwapFilename();
        List<String> sInfoList = Lists.newArrayList();
        sInfoList.add(data.getClientMultisigInfo());
        sInfoList.add(data.getMediatorMakeMultisigInfo());
        logger.info("Opening swap wallet");
        return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwom -> {
            logger.info("Making swap multisig");
            return monero.makeMultisig(sInfoList).flatMap(smm -> {
                data.setSwapFinalizeMultisigInfo(smm.getResult().getMultisig_info());
                logger.info("Closing swap wallet");
                return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(scwcm -> {
                    return Mono.just(data);
                });
            });
        });
    }

    /**
     * Helper method for finalizing mediator swap wallet multisig
     * @param data
     * @return Mono<FinalizeResponse>
     */
    public Mono<FinalizeResponse> finalizeMediatorMultisig(FundRequest request) {
        XmrQuoteTable table = moneroQuoteRepository.findById(request.getHash()).get();
        String mfn = table.getMediator_filename();
        List<String> mInfoList = Lists.newArrayList();
        mInfoList.add(request.getMakeMultisigInfo());
        mInfoList.add(table.getSwap_finalize_msig());
        logger.info("Opening mediator wallet");
        return monero.controlWallet(WalletState.OPEN, mfn).flatMap(mcwo -> {
            logger.info("Finalizing mediator multisig");
            return monero.finalizeMultisig(mInfoList).flatMap(sfm -> {
                logger.info("Closing mediator wallet");
                return monero.controlWallet(WalletState.CLOSE, mfn).flatMap(mcwc -> {
                    return finalizeSwapMultisig(request, table);
                });
            });
        });
    }

    /**
     * Helper method for finalizing main swap wallet multisig
     * @param data
     * @return Mono<FinalizeResponse>
     */
    private Mono<FinalizeResponse> finalizeSwapMultisig(FundRequest request, XmrQuoteTable table) {
        String sfn = table.getSwap_filename();
        List<String> sInfoList = Lists.newArrayList();
        sInfoList.add(request.getMakeMultisigInfo());
        sInfoList.add(table.getMediator_finalize_msig());
        logger.info("Opening swap wallet");
        return monero.controlWallet(WalletState.OPEN, sfn).flatMap(scwo -> {
            logger.info("Finalizing swap multisig");
            return monero.finalizeMultisig(sInfoList).flatMap(sfm -> {
                logger.info("Closing swap wallet");
                return monero.controlWallet(WalletState.CLOSE, sfn).flatMap(scwc -> {
                    return Mono.just(sfm);
                });
            });
        });
    }

    /**
     * Extra step for adding the multisig info which facilitates spending from the
     * consensus wallet. WebFlux chained as: export_multisig (Swap wallet) =>
     * export_multisig (Mediator wallet).
     * @param request
     * @param table
     * @return Mono<FundResponse> //TODO: create new InitResponse
     */
    public Mono<FundResponse> exportSwapInfo(FundRequest request, XmrQuoteTable table) {
        logger.info("Exporting swap info");
        String swapFilename = table.getSwap_filename();
        return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwom -> {
            return monero.exportMultisigInfo().flatMap(sem -> {
                table.setSwap_export_msig_info(sem.getResult().getInfo());
                FundResponse fundResponse = FundResponse.builder()
                    .importSwapMultisigInfo(sem.getResult().getInfo()).build();
                return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(swcc -> {
                    return exportMediatorInfo(request, table, fundResponse);
                });
            });
        });
    }

    /**
     * Helper method for exporting mediator multisig wallet info.
     * This is a necessary process for being able to spend from the 
     * consensus wallet.
     * @param request
     * @param table
     * @return Mono<String> - the address of the finalized multisig wallet
     */
    private Mono<FundResponse> exportMediatorInfo(FundRequest request, XmrQuoteTable table, 
    FundResponse fundResponse) {
        logger.info("Exporting mediator info");
        String mediatorFilename = table.getMediator_filename();
        return monero.controlWallet(WalletState.OPEN, mediatorFilename).flatMap(mcwo -> {
            return monero.exportMultisigInfo().flatMap(mem -> {
                table.setMediator_export_msig_info(mem.getResult().getInfo());
                moneroQuoteRepository.save(table);
                fundResponse.setImportMediatorMultisigInfo(mem.getResult().getInfo());
                return monero.controlWallet(WalletState.CLOSE, mediatorFilename).flatMap(mcwc -> {
                    return Mono.just(fundResponse);
                });
            });
        });
    }
    
// TODO: debug null pointer on import info

    /**
     * Extra step for adding the client multisig info which facilitates spending from the
     * consensus wallet.
     * @param request
     * @param table
     * @return Mono<FundResponse>
     */
    public Mono<ImportInfoResponse> importSwapInfo(SwapRequest request, XmrQuoteTable table) {
        String swapFilename = table.getSwap_filename();
        return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwom -> {
            List<String> sInfoList = Lists.newArrayList();
            sInfoList.add(table.getMediator_export_msig_info());
            // mediator check
            String clientExportInfo = request.getExportMultisigInfo();
            if(clientExportInfo != null) {
                sInfoList.add(request.getExportMultisigInfo());
            }
            return monero.importMultisigInfo(sInfoList).flatMap(sim -> {
                return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(swcc -> {
                    return importMediatorInfo(request, table);
                });
            });
        });
    }

    /**
     * Helper method for importing client info to mediator multisig wallet.
     * This is a necessary process for being able to spend from the 
     * consensus wallet.
     * @param request
     * @param table
     * @return Mono<String> - the address of the finalized multisig wallet
     */
    private Mono<ImportInfoResponse> importMediatorInfo(SwapRequest request, XmrQuoteTable table) {
        String mediatorFilename = table.getMediator_filename();
        return monero.controlWallet(WalletState.OPEN, mediatorFilename).flatMap(mcwo -> {
            List<String> mInfoList = Lists.newArrayList();
            mInfoList.add(table.getSwap_export_msig_info());
            String clientExportInfo = request.getExportMultisigInfo();
            // mediator check
            if(clientExportInfo != null) {
                mInfoList.add(request.getExportMultisigInfo());
            }
            mInfoList.add(request.getExportMultisigInfo());
            return monero.controlWallet(WalletState.CLOSE, mediatorFilename).flatMap(mcwc -> {
                return monero.importMultisigInfo(mInfoList);
            });
        });
    }

}
