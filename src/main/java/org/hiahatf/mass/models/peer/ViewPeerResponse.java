package org.hiahatf.mass.models.peer;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the ViewPeerResponse
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewPeerResponse {
    private List<Peer> peers;
}
