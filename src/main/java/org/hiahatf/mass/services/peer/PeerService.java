package org.hiahatf.mass.services.peer;

import java.text.MessageFormat;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.hiahatf.mass.exception.MassException;
import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.peer.AddPeer;
import org.hiahatf.mass.models.peer.Peer;
import org.hiahatf.mass.models.peer.ViewPeerResponse;
import org.hiahatf.mass.repo.PeerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.transport.ProxyProvider.Proxy;

/**
 * Class for processing peer additions and validations.
 */
@Service(Constants.PEER_SERVICE)
public class PeerService {

    private Logger logger = LoggerFactory.getLogger(PeerService.class);
    // discover new peers every
    private static final int DISCOVER_FREQUENCY = 3600000;
    private static final int DISCOVER_INITIAL_DELAY = 60000;
    // update peers every 7200 sec.
    private static final int UPDATE_FREQUENCY = 7200000;
    private static final int UPDATE_INITIAL_DELAY = 3600000;
    private PeerRepository peerRepository;
    private boolean isSharingPeers;
    private String seedNode;
    private String massId;

    /**
     * Peer Service DI
     * @param peerRepository
     */
    @Autowired
    public PeerService(PeerRepository peerRepository,
        @Value(Constants.IS_SHARING_PEERS) boolean isSharingPeers,
        @Value(Constants.SEED_NODE) String seedNode,
        @Value(Constants.MASS_ID) String massId) {
        this.peerRepository = peerRepository;
        this.isSharingPeers = isSharingPeers;
        this.seedNode = seedNode;
        this.massId = massId;
    }

    /**
     * Exchange peer identification if max peers is not breached
     * and base32 validation is successful.
     * @param request
     * @return AddPeer
     */
    public Mono<AddPeer> addPeer(AddPeer request) {
        Iterable<Peer> peers = peerRepository.findAll();
        long peerCount = StreamSupport.stream(peers.spliterator(), false).count();
        if(peerCount >= Constants.MAX_PEERS) {
            return Mono.error(new MassException(Constants.MAX_PEER_ERROR));
        }
        // base32 validation
        String peerId = request.getPeerId();
        boolean isValidPeer = Pattern.compile(Constants.BASE32_REGEX)
            .matcher(peerId).matches()
            && peerId.length() % 8 == 0;
        if(!isValidPeer) {
            return Mono.error(new MassException(Constants.INVALID_PEER_ERROR));
        }
        Peer peer = Peer.builder().peer_id(peerId).is_active(true)
            .is_malicous(false).is_vetted(false).build();
        peerRepository.save(peer);
        AddPeer response = AddPeer.builder().build();
        return Mono.just(response);
    }

    /**
     * Share peers if allowed by peer.share=true and
     * associate p2p information
     * @return ViewPeerResponse
     */
    public Mono<ViewPeerResponse> viewPeer() {
        if(!isSharingPeers) {
            return Mono.error(new MassException(Constants.PEER_SHARE_ERROR));
        }
        Iterable<Peer> peers = peerRepository.findAll();
        Flux<Peer> peerFlux = Flux.fromIterable(peers);
        ViewPeerResponse response = ViewPeerResponse.builder().peers(peerFlux).build();
        return Mono.just(response);
    }

    /**
     * This method updates peer status as active every 7200s by pinging
     * the peer's /health API.
     */
    @Scheduled(initialDelay = UPDATE_INITIAL_DELAY, fixedDelay = UPDATE_FREQUENCY)
    public void updatePeerStatus() {
        logger.info(Constants.PEER_UPDATE_MSG);
        Iterable<Peer> peers = peerRepository.findAll();
        Flux<Peer> peerFlux = Flux.fromIterable(peers);
        Peer updatePeer = Peer.builder().build();
        peerFlux.subscribe(p -> {
            String pid = p.getPeer_id();
            buildPeerProxy(pid).get().uri(uri -> uri.path(Constants.HEALTH_PATH).build())
                .retrieve().toBodilessEntity().subscribe(r -> {
                if(r.getStatusCode() != HttpStatus.OK) {
                    logger.info(Constants.PEER_INACTIVE_MSG, pid);
                    updatePeer.set_active(false);
                    peerRepository.save(updatePeer);
                } else {
                    // if they are inactive on the second check remove them
                    if(!p.is_active()) {
                        peerRepository.delete(p);
                    }
                    updatePeer.set_active(false);
                    logger.info(Constants.PEER_ACTIVE_MSG, pid);
                }
                updatePeerBehavior(p, pid);
            });
        });
    }

    /**
     * This method updates peer status as active every 3600s by pinging
     * the peer's /peer/view API.
     */
    @Scheduled(initialDelay = DISCOVER_INITIAL_DELAY, fixedDelay = DISCOVER_FREQUENCY)
    public void discoverPeers() {
        logger.info(Constants.PEER_DISCOVERY_MSG);
        Iterable<Peer> peers = peerRepository.findAll();
        long peerCount = StreamSupport.stream(peers.spliterator(), false).count();
        if(peerCount < Constants.MAX_PEERS) {
            Flux<Peer> peerFlux = Flux.fromIterable(peers);
            peerFlux.subscribe(p -> {
                String pid = p.getPeer_id();
                    executePeerDiscovery(pid);
                });
        }
        // set the seed node and seed nodes peers
        if(peerCount == 0) {
            AddPeer addPeer = AddPeer.builder().peerId(massId).build();
            buildPeerProxy(seedNode).post().uri(uri -> 
                uri.path(Constants.PEER_ADD_PATH).build())
                .bodyValue(addPeer)
                .retrieve().toBodilessEntity().subscribe(r -> {
                    if(r.getStatusCode() == HttpStatus.OK) {
                        logger.info(Constants.SEED_NODE_MSG, seedNode);
                    }
            });
            executePeerDiscovery(seedNode);
        }
    }

    /**
     * Helper method to build the web client proxy and force all p2p communications
     * over the invisible internet project. I2P router must be active.
     * @param peerId
     * @return WebClient
     */
    private WebClient buildPeerProxy(String peerId) {
        String host = MessageFormat.format(Constants.PEER_HOST_FORMAT, peerId);
        HttpClient httpClient = HttpClient.create()
            .proxy(proxy -> proxy.type(Proxy.HTTP).host(Constants.PEER_PROXY));
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
        return WebClient.builder().baseUrl(host).clientConnector(connector).build();
    }

    /**
     * Reusable method for executing peer discovery on current peers
     * or seeding the server on startup 
     * @param peerId
     */
    private void executePeerDiscovery(String peerId) {
        buildPeerProxy(peerId).get().uri(uri -> uri.path(Constants.PEER_VIEW_PATH).build())
            .retrieve().bodyToMono(ViewPeerResponse.class).subscribe(r -> {
                r.getPeers().subscribe(peer -> {
                    if(!peer.is_malicous() && peer.is_vetted()) {
                        logger.info(Constants.PEER_ADDED_MSG, peerId);
                        peerRepository.save(peer);
                    }
                });
            });
    }

    /**
     * Update peers whom have had excessive cancelled swaps or peers
     * that have reaching 3 consecutive successful swaps.
     * @param p
     * @param peerId
     */
    private void updatePeerBehavior(Peer p, String peerId) {
        if(p.getCancel_counter() == Constants.PEER_PERFORMANCE_THESHOLD) {
            logger.info(Constants.PEER_MALICIOUS_MSG, peerId);
            Peer maliciousPeer = Peer.builder().is_malicous(true).build();
            peerRepository.save(maliciousPeer);
        }
        if(p.getSwap_counter() == Constants.PEER_PERFORMANCE_THESHOLD) {
            logger.info(Constants.PEER_VETTED_MSG, peerId);
            Peer vettedPeer = Peer.builder().is_vetted(true).build();
            peerRepository.save(vettedPeer);
        }
    }
    
}
