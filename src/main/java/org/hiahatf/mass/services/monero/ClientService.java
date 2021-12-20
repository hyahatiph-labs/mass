package org.hiahatf.mass.services.monero;

import java.text.MessageFormat;
import java.util.List;

import com.google.common.collect.Lists;

import org.apache.commons.codec.binary.Hex;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.monero.MoneroQuote;
import org.hiahatf.mass.models.monero.MultisigData;
import org.hiahatf.mass.models.monero.Quote;
import org.hiahatf.mass.models.monero.Request;
import org.hiahatf.mass.models.monero.wallet.WalletState;
import org.hiahatf.mass.repo.MoneroQuoteRepository;
import org.hiahatf.mass.services.bitcoin.QuoteService;
import org.hiahatf.mass.services.peer.PeerService;
import org.hiahatf.mass.services.rpc.Monero;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

// TODO: testing for client quote processing over i2p

/**
 * Class for handling all Monero client logic
 */
@Service(Constants.XMR_ClIENT_SERVICE)
public class ClientService {
    
    private Logger logger = LoggerFactory.getLogger(ClientService.class);

    private MoneroQuoteRepository quoteRepository;
    public static boolean isWalletOpen;
    private QuoteService quoteService;
    private PeerService peerService;
    private String seedNode;
    private Monero monero;

    /**
     * Client service dependency injection
     */
    @Autowired
    public ClientService(PeerService peerService, QuoteService quoteService, 
        @Value(Constants.SEED_NODE) String seedNode, MoneroQuoteRepository quoteRepository, 
        Monero monero) {
            this.quoteRepository = quoteRepository;
            this.quoteService = quoteService;
            this.peerService = peerService;
            this.seedNode = seedNode;
            this.monero = monero;
    }

    /**
     * Relay the quote 
     * @param request
     * @return
     */
    public Mono<Quote> relayQuote(Request request) {
        MultisigData data = MultisigData.builder().build();
        // get the preimage
        byte[] preimage = quoteService.createPreimage();
        byte[] bHash = quoteService.createPreimageHash(preimage);
        String hash = Hex.encodeHexString(bHash);
        Mono<Quote> tempQuote = createConsensusWallet(request, hash, data).flatMap(pmi -> {
            request.setMultisigInfo(pmi.getClientMultisigInfo());
            request.setPeerId(seedNode);
            request.setPreimageHash(bHash);
            // TODO: peer selection algorithm, for now use seed node for development
            return peerService.buildPeerProxy(seedNode).post().uri(uri -> 
                uri.path(Constants.XMR_QUOTE_PATH).build()).bodyValue(request)
                .retrieve().bodyToMono(Quote.class);
            });
        makeConsensusWallet(data, tempQuote, request);
        return tempQuote;
    }

    /**
     * Create the client wallet for controlling the swap
     * @param request
     * @param hash
     * @param data
     * @return
     */
    private Mono<MultisigData> createConsensusWallet(Request request, String hash, MultisigData data) {
        long unixTime = System.currentTimeMillis() / 1000L;
        String format = "{0}{1}";
        String clientFilename = MessageFormat.format(format, hash, String.valueOf(unixTime));
        logger.info("Client filename: {}", clientFilename);
        data.setSwapFilename(clientFilename);
        logger.info("Creating client wallet");
        return monero.createWallet(clientFilename).flatMap(sfn -> {
            return monero.controlWallet(WalletState.OPEN, clientFilename).flatMap(scwo -> {
                logger.info("Opening client wallet");
                return monero.prepareMultisig().flatMap(spm -> {
                    String info = spm.getResult().getMultisig_info();
                    data.setClientMultisigInfo(info);
                    persistQuote(request, data, hash);
                    return Mono.just(data);
                });
            });
        });     
    }

    /**
     * With response from swap server, Call make_multisig_info
     * on the client wallet and persist to DB.
     * @param data
     * @param quote
     * @param request
     */
    private void makeConsensusWallet(MultisigData data, Mono<Quote> quote, Request request) {
        quote.subscribe(q -> {
            List<String> infos = Lists.newArrayList();
            infos.add(q.getMediatorMakeMultisigInfo());
            infos.add(q.getSwapMakeMultisigInfo());
            monero.makeMultisig(infos).subscribe(mmi -> {
                data.setSwapMakeMultisigInfo(mmi.getResult().getMultisig_info());
                persistQuote(request, data, q.getQuoteId());
                logger.info("Made client wallet for : {}", q.getQuoteId());
                monero.controlWallet(WalletState.CLOSE, data.getSwapFilename()).subscribe(ccw -> {
                    logger.info("Closing client walet: {}", q.getQuoteId());
                });
            });
        });
    }

    /**
     * Persist the Client's MoneroQuote to the database for future processing.
     * @param request - client request
     * @param data - data for consensus wallet
     * @param hash - preimage hash
     */
    private void persistQuote(Request request, MultisigData data, String hash) {
        // store in db to settle the invoice later 
        MoneroQuote quote = MoneroQuote.builder()
            .amount(request.getAmount()).dest_address(request.getAddress())
            .mediator_filename(data.getMediatorFilename())
            .mediator_finalize_msig(data.getMediatorFinalizeMultisigInfo())
            .swap_finalize_msig(data.getSwapFinalizeMultisigInfo())
            .payment_hash(request.getPreimageHash())
            .peer_id(request.getPeerId())
            .swap_filename(data.getSwapFilename())
            .quote_id(hash)
            .build();
        quoteRepository.save(quote);
    }
    
}
