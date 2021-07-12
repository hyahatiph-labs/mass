package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero transfer request.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroTransferRequest {
    private final String jsonrpc = "2.0";
    private final String id = "0";
    private final String method = "transfer";
    private MoneroTransferParameters params;   
}
