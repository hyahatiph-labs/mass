package org.hiahatf.mass.models.monero.multisig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the sweep_all parameters.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SweepAllParameters {
    private String address;
    private final int account_index = 0;
    private final int[] subaddr_indices = {0};
    private final int priority = 0;
    private final int ring_size = 11;
    private final boolean do_not_relay = true;
}
