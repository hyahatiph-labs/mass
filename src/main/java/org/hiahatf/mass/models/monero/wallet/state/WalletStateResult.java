package org.hiahatf.mass.models.monero.wallet.state;

import lombok.Builder;
import lombok.Data;

/**
 * POJO for the open/close_wallet response. 
 * See Monero RPC docs for more details.
 */
@Data
@Builder
public class WalletStateResult {
    // this RPC method has empty result
}
