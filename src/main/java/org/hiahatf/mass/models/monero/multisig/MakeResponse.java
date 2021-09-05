package org.hiahatf.mass.models.monero.multisig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the make_multisig result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeResponse {
    private String id;
    private String jsonrpc;
    private MakeResult result;
}
