package org.hiahatf.mass.models.monero.proof;

import org.hiahatf.mass.models.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the get_spend_proof request.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetSpendProofRequest {
    private final String jsonrpc = Constants.XMR_RPC_VER;
    private final String id = Constants.XMR_RPC_ID;
    private final String method = Constants.XMR_RPC_GET_SPEND_PROOF;
    private GetSpendProofParameters params;
}
