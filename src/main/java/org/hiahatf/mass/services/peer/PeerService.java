package org.hiahatf.mass.services.peer;

import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableList;

import org.hiahatf.mass.exception.MassException;
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
        Iterable<Peer> peers = peerRepository.findAll();
        List<Peer> peerList = ImmutableList.copyOf(peers);
        if(peerList.size() >= Constants.MAX_PEERS) {
            return Mono.error(new MassException(Constants.MAX_PEER_ERROR));
        }
        // base32 validation
        String b32RegEx = "/^[A-Z2-7]+=*$/";
        String peerId = request.getPeerId();
        boolean isValidPeer = Pattern.compile(b32RegEx).matcher(peerId).matches()
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
     * Share peers if allowed by share.peer=true and
     * associate p2p information
     * @return ViewPeerResponse
     */
    public Mono<ViewPeerResponse> viewPeer() {
        Iterable<Peer> peers = peerRepository.findAll();
        List<Peer> peerList = ImmutableList.copyOf(peers);
        ViewPeerResponse response = ViewPeerResponse.builder().peers(peerList).build();
        return Mono.just(response);
    }

    // TODO: p2p discovery, vetting and sharing with spring scheduler

}
