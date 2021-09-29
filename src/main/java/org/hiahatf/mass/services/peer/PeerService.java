package org.hiahatf.mass.services.peer;

import java.util.List;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.peer.AddPeer;
import org.hiahatf.mass.models.peer.Peer;
import org.hiahatf.mass.models.peer.ViewPeerResponse;
import org.hiahatf.mass.repo.PeerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

/**
 * Class for processing peer additions and validations.
 */
@Service(Constants.PEER_SERVICE)
public class PeerService {
    
    private PeerRepository peerRepository;

    /**
     * Peer Service DI
     * @param peerRepository
     */
    @Autowired
    public PeerService(PeerRepository peerRepository) {
        this.peerRepository = peerRepository;
    }

    /**
     * Exchange peer identification if max peers is not breached
     * and base32 validation is successful.
     * @param request
     * @return AddPeer
     */
    public Mono<AddPeer> addPeer(AddPeer request) {
        AddPeer response = AddPeer.builder().build();
        return Mono.just(response);
    }

    /**
     * Share peers if allowed by share.peer=true and
     * associate p2p information
     * @return ViewPeerResponse
     */
    public Mono<ViewPeerResponse> viewPeer() {
        Iterable<Peer> peers = peerRepository.findAll();
        ViewPeerResponse response = ViewPeerResponse.builder()
            .peers(peers).build();
        return Mono.just(response);
    }

    // TODO: base32 validation

    // TODO: max peer and share peer checks

}
