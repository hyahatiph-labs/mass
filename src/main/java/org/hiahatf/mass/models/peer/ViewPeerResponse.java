package org.hiahatf.mass.models.peer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * POJO for the ViewPeerResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewPeerResponse {
    private Flux<Peer> peers;
}
