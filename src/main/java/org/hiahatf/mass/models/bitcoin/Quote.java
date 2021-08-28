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
    private String sendAddress;
    private Double rate;
    private Long minSwapAmt;
    private Long maxSwapAmt;
    // prepare multisig information outputs client needs this to do make_multisig
    private String swapMakeMultisigInfo;
    private String mediatorMakeMultisigInfo;
    // make multisig information outputs client needs this to do finalize_multisig
    private String swapFinalizeMultisigInfo;
    private String mediatorFinalizeMultisigInfo;
}
