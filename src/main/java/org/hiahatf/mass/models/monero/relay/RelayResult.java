package org.hiahatf.mass.models.monero.relay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the relay_tx result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelayResult {
    private RelayResult result;
}
