package org.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Swap Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapResponse {
    // payment hash / quoteId
    private String hash;
    // spend proof of the xmr transfer
    private String spend_proof;
}
