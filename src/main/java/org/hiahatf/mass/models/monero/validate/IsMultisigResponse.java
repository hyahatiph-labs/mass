package org.hiahatf.mass.models.monero.validate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the is_multisig response.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsMultisigResponse {
    private String id;
    private String jsonrpc;
    private IsMultisigResult result;
}
