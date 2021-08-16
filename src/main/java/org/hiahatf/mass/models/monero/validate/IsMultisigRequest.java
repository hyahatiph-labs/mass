package org.hiahatf.mass.models.monero.validate;

import org.hiahatf.mass.models.Constants;

import lombok.Builder;
import lombok.Data;

/**
 * POJO for the is_multisig request.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
public final class IsMultisigRequest {
    private final String jsonrpc = Constants.XMR_RPC_VER;
    private final String id = Constants.XMR_RPC_ID;
    private final String method = Constants.XMR_RPC_IS_MULTISIG;
}
