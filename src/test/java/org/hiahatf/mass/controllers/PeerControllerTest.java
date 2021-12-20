package org.hiahatf.mass.controllers;

import static org.mockito.Mockito.when;

import org.hiahatf.mass.models.peer.AddPeer;
import org.hiahatf.mass.models.peer.Peer;
import org.hiahatf.mass.services.peer.PeerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class PeerControllerTest {
    
    @Mock
    private PeerService peerService;
    @InjectMocks
    private PeerController peerController;

    @Test
    @DisplayName("Add Peer Controller Test")
    public void addPeerTest() {
        String peerId = "peer123";
        AddPeer aPeer = AddPeer.builder().peerId(peerId).build();
        when(peerService.addPeer(aPeer)).thenReturn(Mono.just(aPeer));
        Mono<AddPeer> testPeer = peerController.addPeer(aPeer);
        StepVerifier.create(testPeer)
        .expectNextMatches(r -> r.getPeerId()
          .equals(peerId))
        .verifyComplete();
    }

    @Test
    @DisplayName("Add Peer Controller Test")
    public void viewPeerTest() {
        String peerId = "peer123";
        Peer expectedPeer = Peer.builder().peer_id(peerId).build();
        when(peerService.viewPeer()).thenReturn(Flux.just(expectedPeer));
        Flux<Peer> peerFlux = peerController.viewPeer();
        StepVerifier.create(peerFlux)
        .expectNextMatches(r -> r.getPeer_id()
          .equals(peerId))
        .verifyComplete();
    }

}
