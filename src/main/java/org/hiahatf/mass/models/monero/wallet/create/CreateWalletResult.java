package org.hiahatf.mass.models.monero.wallet.create;

import lombok.Builder;
import lombok.Data;

/**
 * POJO for the create_wallet result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
public class CreateWalletResult {
    // this RPC method returns empty data
}
