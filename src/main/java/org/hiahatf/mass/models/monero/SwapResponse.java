package org.hiahatf.mass.models.monero;

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
    // metadata needed to relay tx
    private String metadata;
}
