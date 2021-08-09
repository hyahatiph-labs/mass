package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for response of funding the mass consensus wallet
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FundResponse {
    private String importSwapMultisigInfo;
    private String importMediatorMultisigInfo;
    private String txid;
}
