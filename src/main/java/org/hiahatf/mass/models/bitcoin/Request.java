package org.hiahatf.mass.models.bitcoin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Bitcoin quote request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    private String paymentRequest;
    private String refundAddress;
}
