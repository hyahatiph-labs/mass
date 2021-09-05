package org.hiahatf.mass.models.monero.multisig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the sign_multisig response.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignResponse {
    private String id;
    private String jsonrpc;
    private SignResult result;
}
