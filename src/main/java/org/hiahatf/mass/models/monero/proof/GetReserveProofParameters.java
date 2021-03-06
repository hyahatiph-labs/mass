package org.hiahatf.mass.models.monero.proof;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the get_reserve_proof params.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetReserveProofParameters {
    private boolean all;
    private final int account_index = 0;
    private long amount;
}
