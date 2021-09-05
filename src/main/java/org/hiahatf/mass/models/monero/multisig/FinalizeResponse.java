package org.hiahatf.mass.models.monero.multisig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the finalize_multisig response.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeResponse {
    private String id;
    private String jsonrpc;
    private FinalizeResult result;
}
