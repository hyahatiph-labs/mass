package org.hiahatf.mass.services.peer;

import java.text.MessageFormat;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

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
import reactor.netty.http.server.ProxyProtocolSupportType;
import reactor.netty.transport.ProxyProvider.Proxy;

/**
 * Class for processing peer additions and validations.
 */
@Service(Constants.PEER_SERVICE)
public class PeerService {

    // update peers every 7200 sec.
    private Logger logger = LoggerFactory.getLogger(PeerService.class);
    private static final int INITIAL_DELAY = 86400000;
    private static final int UPDATE_FREQUENCY = 7200000;
    private static final int DISCOVER_FREQUENCY = 3600000;
    private PeerRepository peerRepository;
    private boolean isSharingPeers;

    /**
     * Peer Service DI
     * @param peerRepository
     */
    @Autowired
    public PeerService(PeerRepository peerRepository,
        @Value(Constants.IS_SHARING_PEERS) boolean isSharingPeers) {
        this.peerRepository = peerRepository;
        this.isSharingPeers = isSharingPeers;
    }

    /**
     * Exchange peer identification if max peers is not breached
     * and base32 validation is successful.
     * @param request
     * @return AddPeer
     */
    public Mono<AddPeer> addPeer(AddPeer request) {
        Iterable<Peer> peers = peerRepository.findAll();
        List<Peer> peerList = ImmutableList.copyOf(peers);
        if(peerList.size() >= Constants.MAX_PEERS) {
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
    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = UPDATE_FREQUENCY)
    public void updatePeerStatus() {
        logger.info(Constants.PEER_UPDATE_MSG);
        Iterable<Peer> peers = peerRepository.findAll();
        Flux<Peer> peerFlux = Flux.fromIterable(peers);
        Peer updatePeer = Peer.builder().build();
        peerFlux.subscribe(p -> {
            String pid = p.getPeer_id();
            WebClient client = buildPeerProxy(pid);
            client.get().uri(uri -> uri.path(Constants.HEALTH_PATH).build())
                .retrieve().toBodilessEntity().subscribe(r -> {
                if(r.getStatusCode() != HttpStatus.OK) {
                    logger.info(Constants.PEER_INACTIVE_MSG, pid);
                    updatePeer.set_active(true);
                    peerRepository.save(updatePeer);
                } else {
                    // if they are inactive on the second check remove them
                    if(!p.is_active()) {
                        peerRepository.delete(p);
                    }
                    updatePeer.set_active(false);
                    logger.info(Constants.PEER_ACTIVE_MSG, pid);
                }
            });
        });
    }

    /**
     * This method updates peer status as active every 3600s by pinging
     * the peer's /peer/view API.
     */
    @Scheduled(initialDelay = INITIAL_DELAY, fixedDelay = DISCOVER_FREQUENCY)
    public void discoverPeers() {
        logger.info(Constants.PEER_DISCOVERY_MSG);
        Iterable<Peer> peers = peerRepository.findAll();
        Flux<Peer> peerFlux = Flux.fromIterable(peers);
        peerFlux.subscribe(p -> {
            String pid = p.getPeer_id();
            WebClient client = buildPeerProxy(pid);
            client.get().uri(uri -> uri.path(Constants.PEER_VIEW_PATH).build())
                .retrieve().bodyToMono(ViewPeerResponse.class).subscribe(r -> {
                r.getPeers().subscribe(peer -> {
                    if(!peer.is_malicous() && peer.is_vetted()) {
                        logger.info(Constants.PEER_ADDED_MSG, pid);
                        peerRepository.save(peer);
                    }
                });
            });
        });
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

    // TODO: add peer vetting logic to swap and mediator services

    // TODO: add peer id to quote request and DB persistence

    // TODO: reject quote request if peer not added

    // TODO: verify peers comms over i2p

    // TODO: junit
}
