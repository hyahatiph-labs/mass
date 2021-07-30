package org.hiahatf.mass.models.monero.wallet.state;

import org.hiahatf.mass.models.Constants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the open/close_wallet request.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletStateRequest {
    private final String jsonrpc = Constants.XMR_RPC_VER;
    private final String id = Constants.XMR_RPC_ID;
    private String method;
    private WalletStateParameters params;
}
