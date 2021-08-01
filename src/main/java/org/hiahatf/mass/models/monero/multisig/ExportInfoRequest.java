package org.hiahatf.mass.models.monero.multisig;

import org.hiahatf.mass.models.Constants;

import lombok.Builder;
import lombok.Data;

/**
 * POJO for the export_multisig_info request.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
public class ExportInfoRequest {
    private final String jsonrpc = Constants.XMR_RPC_VER;
    private final String id = Constants.XMR_RPC_ID;
    private final String method = Constants.XMR_RPC_EXPORT_MSIG_INFO;
}
