package org.hiahatf.mass.models.monero.proof;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the check_reserve_proof response.
 * See Monero RPC docs for more info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckReserveProofResponse {
    private String id;
    private String jsonrpc;
    private CheckReserveProofResult result;
}
