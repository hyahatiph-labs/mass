package org.hiahatf.mass.models.peer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the AddPeer Request / Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddPeer {
    private String peerId;
}
