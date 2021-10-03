package org.hiahatf.mass.service.peer;

import static org.mockito.Mockito.when;

import java.util.List;

import com.google.common.collect.Lists;

import org.hiahatf.mass.models.peer.AddPeer;
import org.hiahatf.mass.models.peer.Peer;
import org.hiahatf.mass.repo.PeerRepository;
import org.hiahatf.mass.services.peer.PeerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import okhttp3.mockwebserver.MockWebServer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for all p2p logic
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
public class PeerServiceTest {
    
    private String seedId = "seed.b32.i2p";
    private String massId = "mass.b32.i2p";
    public static MockWebServer mockBackEnd;

    @Mock
    ResponseEntity<Void> entity;    
    @Mock
    private PeerRepository peerRepository;
    @InjectMocks
    PeerService peerService = new PeerService(peerRepository, true, seedId, massId);

    @Test
    @DisplayName("Add Peer Service Test")
    public void addPeerTest() {
        AddPeer request = AddPeer.builder().peerId("peer123.b32.i2p").build();
        Mono<AddPeer> testResponse = peerService.addPeer(request);
        StepVerifier.create(testResponse)
        .expectNextMatches(r -> r.getPeerId()
          .equals(massId))
        .verifyComplete();
    }

    @Test
    @DisplayName("View Peer Service Test")
    public void viewPeerTest() {
        String expectedId = "peer123.b32.i2p";
        Peer peer = Peer.builder().peer_id(expectedId).build();
        List<Peer> peers = Lists.newArrayList();
        peers.add(peer);
        when(peerRepository.findAll()).thenReturn(peers);
        Flux<Peer> peerFlux = peerService.viewPeer();
        StepVerifier.create(peerFlux)
        .expectNextMatches(r -> r.getPeer_id()
          .equals(expectedId))
        .verifyComplete();
    }

    @Test
    @DisplayName("Update Peer Service Test")
    public void updatePeerTest() {
        String expectedId = "peer123.b32.i2p";
        Peer peer = Peer.builder().peer_id(expectedId).active(true).build();
        List<Peer> peers = Lists.newArrayList();
        peers.add(peer);
        when(peerRepository.findAll()).thenReturn(peers);
        peerService.updatePeerStatus();
    }

    @Test
    @DisplayName("Discover Peer Service Test")
    public void discoverPeerTest() {
        String expectedId = "peer123.b32.i2p";
        Peer peer = Peer.builder().peer_id(expectedId).build();
        List<Peer> peers = Lists.newArrayList();
        peers.add(peer);
        when(peerRepository.findAll()).thenReturn(peers);
        peerService.discoverPeers();
    }

    @Test
    @DisplayName("Seed Peer Service Test")
    public void seedPeerTest() {
        List<Peer> peers = Lists.newArrayList();
        when(peerRepository.findAll()).thenReturn(peers);
        peerService.discoverPeers();
    }

}
