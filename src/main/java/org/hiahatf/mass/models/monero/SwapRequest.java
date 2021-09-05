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
    // quoteId
    private String hash;
    // preimage to settle invoice
    private byte[] preimage;
}
