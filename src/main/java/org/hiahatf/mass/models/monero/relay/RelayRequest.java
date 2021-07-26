package org.hiahatf.mass.models.monero.relay;

import org.hiahatf.mass.models.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the relay_tx request.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelayRequest {
    private final String jsonrpc = Constants.XMR_RPC_VER;
    private final String id = Constants.XMR_RPC_ID;
    private final String method = Constants.XMR_RPC_TRANSFER;
    private RelayParameters params;
}
