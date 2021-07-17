package org.hiahatf.mass.models.bitcoin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Bitcoin swap request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapRequest {
    private String paymentRequest;
    private String metadata;
}
