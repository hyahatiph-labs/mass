package org.hiahatf.mass.models.monero.wallet.state;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the open/close_wallet response. 
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletStateResponse {
    private String id;
    private String jsonrpc;
    private WalletStateResult result;
}
