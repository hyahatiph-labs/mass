package org.hiahatf.mass.models.lightning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the decoded payment request.
 * See lightning API for more info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String payment_hash;
    private String num_satoshis;
    private String expiry;
}
