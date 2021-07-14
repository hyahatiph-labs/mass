package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero transfer response.
 * See Monero RPC docs for more info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String id;
    private String jsonrpc;
    private TransferResult result;
}
