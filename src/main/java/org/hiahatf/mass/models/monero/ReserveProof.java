package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the quote proof
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReserveProof {
    // proof signature
    private String signature;
    // proof address
    // configure in application.yml
    // TODO: dynamic configuration
    private String proofAddress;
}
