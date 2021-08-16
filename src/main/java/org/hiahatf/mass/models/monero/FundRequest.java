package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for request of funding the mass consensus wallet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundRequest {
    private String hash;
    private String exportMultisigInfo;
}
