package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Swap Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapRequest {
    // payment hash / quoteId
    private String hash;
    // export multisig info from client
    private String exportMultisigInfo;
}
