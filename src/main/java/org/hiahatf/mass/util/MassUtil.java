package org.hiahatf.mass.util;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import javax.net.ssl.SSLException;

import com.google.common.collect.Lists;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.LiquidityType;
import org.hiahatf.mass.models.monero.FundRequest;
import org.hiahatf.mass.models.monero.InitRequest;
import org.hiahatf.mass.models.monero.InitResponse;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.hiahatf.mass.models.monero.multisig.FinalizeResponse;
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
     * 
     * @param markup
     */
    public MassUtil(@Value(Constants.MARKUP) Double markup, @Value(Constants.MIN_PAY) long minPay,
            @Value(Constants.MAX_PAY) long maxPay, Lightning lightning, Monero monero,
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
     * 
     * @param rateString
     * @return Monero rate
     */
    public Double parseMoneroRate(String rateString) {
        Double parsedRate = Double.valueOf(
                rateString.split(Constants.SEMI_COLON_DELIMITER)[1]
                .split(Constants.RIGHT_BRACKET_DELIMITER)[0]);
        Double realRate = (parsedRate * markup) + parsedRate;
        // create the real rate by adding the markup to parsed rate
        logger.info(Constants.PARSE_RATE_MSG, parsedRate, realRate);
        ;
        return realRate;
    }

    /**
     * Perform validations on channel balance to ensure that a payment proposed on
     * the quote MAY possibly be fulfilled.
     * 
     * @param value - satoshi value of invoice
     * @return Mono<Boolean>
     */
    public Mono<Boolean> validateLiquidity(Double value, LiquidityType type) {
        // payment threshold validation
        long lValue = value.longValue();
        boolean isValid = lValue <= maxPay && lValue >= minPay;
        if (!isValid) {
            String error = MessageFormat.format(Constants.PAYMENT_THRESHOLD_ERROR, 
                String.valueOf(minPay), String.valueOf(maxPay));
            return Mono.error(new MassException(error));
        }
        try {
            return lightning.fetchBalance().flatMap(b -> {
                // sum of sats in channels remote balance
                long balance = type == LiquidityType.INBOUND
                    ? Long.valueOf(b.getRemote_balance().getSat())
                    : Long.valueOf(b.getLocal_balance().getSat());
                if (lValue <= balance) {
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
     * This method configures 2/3 multisig using multisig info passed in the
     * request. 1) Create wallet and open wallet for mass swap. 2) Make multisig
     * with clients' prepare multisig info. 3) Prepare multisig to share with
     * mediator and client. 4) Close swap wallet and perform steps 1,2 and 3 with
     * mediator wallet. 5) Pass prepare / make multisig info to final response. 6)
     * Close wallets and handle wallet control.
     * 
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
        MultisigData data = MultisigData.builder().swapFilename(swapFilename)
                .mediatorFilename(mediatorFilename)
                .clientMultisigInfo(multisigInfo).build();
        logger.info("Creating swap wallet");
        return monero.createWallet(swapFilename).flatMap(sfn -> {
            return prepareSwapMultisig(data);
        });
    }

    /**
     * Helper method for preparing main swap wallet multisig
     * 
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
     * 
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> prepareMediatorMultisig(MultisigData data) {
        logger.info("Creating mediator wallet");
        String mfn = data.getMediatorFilename();
        return monero.createWallet(mfn).flatMap(mcw -> {
            logger.info("Opening mediator wallet");
            return monero.controlWallet(WalletState.OPEN, mfn).flatMap(mcwo -> {
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
     * 
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> makeMediatorMultisig(MultisigData data, List<String> infoList) {
        logger.info("Making mediator multisig");
        return monero.makeMultisig(infoList).flatMap(mmm -> {
            data.setMediatorFinalizeMultisigInfo(mmm.getResult().getMultisig_info());
            logger.info("Closing mediator wallet");
            return monero.controlWallet(WalletState.CLOSE, data.getMediatorFilename()).flatMap(mcwc -> {
                return makeSwapMultisig(data);
            });
        });
    }

    /**
     * Helper method for making main swap wallet multisig
     * 
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
     * 
     * @param data
     * @return Mono<FinalizeResponse>
     */
    public Mono<FinalizeResponse> finalizeMediatorMultisig(FundRequest request) {
        MoneroQuote quote = moneroQuoteRepository.findById(request.getHash()).get();
        String mfn = quote.getMediator_filename();
        List<String> mInfoList = Lists.newArrayList();
        mInfoList.add(request.getMakeMultisigInfo());
        mInfoList.add(quote.getSwap_finalize_msig());
        logger.info("Opening mediator wallet");
        return monero.controlWallet(WalletState.OPEN, mfn).flatMap(mcwo -> {
            logger.info("Finalizing mediator multisig");
            return monero.finalizeMultisig(mInfoList).flatMap(sfm -> {
                logger.info("Closing mediator wallet");
                return monero.controlWallet(WalletState.CLOSE, mfn).flatMap(mcwc -> {
                    return finalizeSwapMultisig(request, quote);
                });
            });
        });
    }

    /**
     * Helper method for finalizing main swap wallet multisig
     * 
     * @param data
     * @return Mono<FinalizeResponse>
     */
    private Mono<FinalizeResponse> finalizeSwapMultisig(FundRequest request, MoneroQuote quote) {
        String sfn = quote.getSwap_filename();
        List<String> sInfoList = Lists.newArrayList();
        sInfoList.add(request.getMakeMultisigInfo());
        sInfoList.add(quote.getMediator_finalize_msig());
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
     * export_multisig (Mediator wallet) import_multisig (Swap wallet) =>
     * import_multisig (Mediator wallet).
     * 
     * @param quote
     * @param initRequest
     * @return Mono<InitResponse>
     */
    public Mono<InitResponse> exportSwapInfo(MoneroQuote quote, InitRequest initRequest) {
        logger.info("Exporting swap info");
        String swapFilename = quote.getSwap_filename();
        return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwom -> {
            return monero.exportMultisigInfo().flatMap(sem -> {
                InitResponse initResponse = InitResponse.builder()
                    .hash(initRequest.getHash())
                    .swapExportInfo(sem.getResult().getInfo()).build();
                return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(swcc -> {
                    return exportMediatorInfo(quote, initRequest, initResponse);
                });
            });
        });
    }

    /**
     * Helper method for exporting mediator multisig wallet info.
     * This is a necessary process for being able to spend from the 
     * consensus wallet.
     * 
     * @param quote
     * @param initRequest
     * @param initResponse
     * @return Mono<String> - the address of the finalized multisig wallet
     */
    private Mono<InitResponse> exportMediatorInfo(MoneroQuote quote, InitRequest initRequest,
    InitResponse initResponse) {
        logger.info("Exporting mediator info");
        String mediatorFilename = quote.getMediator_filename();
        return monero.controlWallet(WalletState.OPEN, mediatorFilename).flatMap(mcwo -> {
            return monero.exportMultisigInfo().flatMap(mem -> {
                initResponse.setMediatorExportSwapInfo(mem.getResult().getInfo());
                return monero.controlWallet(WalletState.CLOSE, mediatorFilename).flatMap(mcwc -> {
                    return importSwapInfo(initRequest, quote, initResponse);
                });
            });
        });
    }

    /**
     * Extra step for adding the client multisig info which facilitates spending
     * from the consensus wallet.
     * 
     * @param request
     * @param quote
     * @param initResponse
     * @return Mono<FundResponse>
     */
    private Mono<InitResponse> importSwapInfo(InitRequest initRequest, MoneroQuote quote,
    InitResponse initResponse) {
        logger.info("Importing swap info");
        String swapFilename = quote.getSwap_filename();
        return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwom -> {
            List<String> sInfoList = Lists.newArrayList();
            // mediator check
            String clientExportInfo = initRequest.getImportInfo();
            if (clientExportInfo == Constants.MEDIATOR_CHECK || clientExportInfo == null) {
                logger.info("Getting mediator to import");
                sInfoList.add(initResponse.getMediatorExportSwapInfo());
                return monero.importMultisigInfo(sInfoList).flatMap(sim -> {
                    if(sim.getResult() == null) {
                        return Mono.error(new MassException(Constants.MULTISIG_CONFIG_ERROR));
                    }
                    return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(swcc -> {
                        return importMediatorInfo(quote, initResponse);
                    });
                });
            } else {
                sInfoList.add(initRequest.getImportInfo());
                return monero.importMultisigInfo(sInfoList).flatMap(sim -> {
                    if(sim.getResult() == null) {
                        return Mono.error(new MassException(Constants.MULTISIG_CONFIG_ERROR));
                    }
                    return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(swcc -> {
                        return Mono.just(initResponse);
                    });
                });
            }
        });
    }

    /**
     * Helper method for importing client info to mediator multisig wallet. This is
     * a necessary process for being able to spend from the consensus wallet.
     * 
     * @param quote
     * @param initResponse
     * @return Mono<InitResponse>
     */
    private Mono<InitResponse> importMediatorInfo(MoneroQuote quote, InitResponse initResponse) {
        logger.info("Importing Mediator Info");
        String mediatorFilename = quote.getMediator_filename();
        return monero.controlWallet(WalletState.OPEN, mediatorFilename).flatMap(mcwo -> {
            List<String> mInfoList = Lists.newArrayList();
            mInfoList.add(initResponse.getSwapExportInfo());
            return monero.importMultisigInfo(mInfoList).flatMap(imi -> {
                return monero.controlWallet(WalletState.CLOSE, mediatorFilename).flatMap(mcwc -> {
                    if(imi.getResult() == null) {
                        return Mono.error(new MassException(Constants.MULTISIG_CONFIG_ERROR));
                    }
                    return Mono.just(initResponse);
                });
            });
        });
    }

    /* 
        Reverse multisig logic below. This custom multisig very much replicates the similar
        flow when the server is delivering xmr to the client, however, the reverse logic
        does not require mediation. Hence, rather than attempting to refactor the above code
        a custom flow is created. It could be used for other crypto / assets beyond Bitcoin
        in the future.
    */

    /**
     * This method is similar to configureMultisig but excludes any flow
     * for creating a mediator. It also accepts a list of multisig info
     * which could be used for different types of consensus wallets and 
     * associated thresholds.
     * 
     * @param multisigInfo
     * @param hash
     * @return Mono<MultisigData>
     */
    public Mono<MultisigData> rConfigureMultisig(List<String> multisigInfos, String hash) {
        long unixTime = System.currentTimeMillis() / 1000L;
        String format = "{0}{1}";
        String swapFilename = MessageFormat.format(format, hash, String.valueOf(unixTime));
        logger.info("Swap filename: {}", swapFilename);
        MultisigData data = MultisigData.builder().swapFilename(swapFilename)
                .clientMultisigInfos(multisigInfos).build();
        logger.info("Creating swap wallet");
        return monero.createWallet(swapFilename).flatMap(sfn -> {
            return rPrepareSwapMultisig(data);
        });
    }

        /**
     * Helper method for preparing main swap wallet multisig
     * for reverse-swap logic.
     * 
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> rPrepareSwapMultisig(MultisigData data) {
        String swapFilename = data.getSwapFilename();
        return monero.controlWallet(WalletState.OPEN, swapFilename).flatMap(scwo -> {
            logger.info("Opening swap wallet");
            return monero.prepareMultisig().flatMap(spm -> {
                logger.info("Preparing swap wallet");
                data.setSwapMakeMultisigInfo(spm.getResult().getMultisig_info());
                return rMakeSwapMultisig(data);
            });
        });
    }

    /**
     * Helper method for making main swap wallet multisig
     * for reverse-swap logic.
     * 
     * @param data
     * @return Mono<MultisigData>
     */
    private Mono<MultisigData> rMakeSwapMultisig(MultisigData data) {
        String swapFilename = data.getSwapFilename();
        logger.info("Making swap multisig");
        return monero.makeMultisig(data.getClientMultisigInfos()).flatMap(smm -> {
            data.setSwapFinalizeMultisigInfo(smm.getResult().getMultisig_info());
            logger.info("Closing swap wallet");
            return monero.controlWallet(WalletState.CLOSE, swapFilename).flatMap(scwcm -> {
                return Mono.just(data);
            });
        });
    }

    /**
     * Helper method for finalizing main swap wallet multisig
     * 
     * @param data
     * @return Mono<FinalizeResponse>
     */
    public Mono<FinalizeResponse> rFinalizeSwapMultisig(FundRequest request, String sfn) {
        logger.info("Opening swap wallet");
        return monero.controlWallet(WalletState.OPEN, sfn).flatMap(scwo -> {
            logger.info("Finalizing swap multisig");
            return monero.finalizeMultisig(request.getMakeMultisigInfos()).flatMap(sfm -> {
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
     * import_multisig (Swap wallet).
     * 
     * @param table
     * @param initRequest
     * @return Mono<InitResponse>
     */
    public Mono<InitResponse> rExportSwapInfo(String sfn, 
    org.hiahatf.mass.models.bitcoin.InitRequest initRequest) {
        logger.info("Exporting swap info");
        return monero.controlWallet(WalletState.OPEN, sfn).flatMap(scwom -> {
            return monero.exportMultisigInfo().flatMap(sem -> {
                InitResponse initResponse = InitResponse.builder()
                    .hash(initRequest.getHash())
                    .swapExportInfo(sem.getResult().getInfo()).build();
                return monero.importMultisigInfo(initRequest.getImportInfos()).flatMap(sim -> {
                    if(sim.getResult() == null) {
                        return Mono.error(new MassException(Constants.MULTISIG_CONFIG_ERROR));
                    }
                    return monero.controlWallet(WalletState.CLOSE, sfn).flatMap(swcc -> {
                        return Mono.just(initResponse);
                    });
                });
            });
        });
    }

}
