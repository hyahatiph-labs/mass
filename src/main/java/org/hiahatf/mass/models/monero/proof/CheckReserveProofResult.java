package org.hiahatf.mass.models.monero.proof;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the check_reserve_proof result.
 * See Monero RPC docs for more info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckReserveProofResult {
    private boolean good;
    private long spent;
    private long total;
}
