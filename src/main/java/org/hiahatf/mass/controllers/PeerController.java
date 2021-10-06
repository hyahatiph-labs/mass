package org.hiahatf.mass.controllers;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.peer.AddPeer;
import org.hiahatf.mass.models.peer.Peer;
import org.hiahatf.mass.services.peer.PeerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Entry point for peer logic
 */
@RequestMapping
@RestController(Constants.PEER_CONTROLLER)
public class PeerController extends BaseController {
    
    private PeerService peerService;

    /**
     * PeerController DI
     * @param peerService
     */
    @Autowired
    public PeerController(PeerService peerService) {
        this.peerService = peerService;
    }

    /**
     * Exchange peer information
     * @param request
     * @return AddPeer
     */
    @ResponseStatus(HttpStatus.OK)
    @PostMapping(Constants.PEER_ADD_PATH)
    public Mono<AddPeer> addPeer(@RequestBody AddPeer request) {
        return peerService.addPeer(request);
    }

    /**
     * View peer information
     * @param request
     * @return AddPeer
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(Constants.PEER_VIEW_PATH)
    public Flux<Peer> viewPeer() {
        return peerService.viewPeer();
    }

}
