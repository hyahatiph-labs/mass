package org.hiahatf.mass.models.bitcoin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Bitcoin quote response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    private String quoteId;
    private String paymentRequest;
    private String sendAddress;
    private Double moneroAmt;
    private Double rate;
    private Long minSwapAmt;
    private Long maxSwapAmt;
}
