package com.hiahatf.mass.models.monero;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero transfer request.
 * See Monero RPC docs for more details.
 * TODO: more configurable params
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroTransferParameters {
    private List<Destination> destinations;
    private final int account_index = 0;
    private final int[] subaddr_indices = {0};
    private final int priority = 0;
    private final int ring_size = 11;
    private final boolean get_tx_key = true;
}
