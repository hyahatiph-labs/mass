package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero quote response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Quote {
    
    // amount in monero
    private double amount;
    // monero rx address
    private String destAddress;
    // lightning network invoice to pay
    private String invoice;
    // maximum swap amount in satoshis
    private Long maxSwapAmt;
    // minimum swap amount in satoshis
    private Long minSwapAmt;
    // prepare multisig information outputs client needs this to do make_multisig
    private String swapMakeMultisigInfo;
    private String mediatorMakeMultisigInfo;
    // make multisig information outputs client needs this to do finalize_multisig
    private String swapFinalizeMultisigInfo;
    private String mediatorFinalizeMultisigInfo;
    // quote id is the same as lnd payment hash  
    private String quoteId;
    // this the mass rate including markup?
    private Double rate;
    // reserve proof
    private ReserveProof reserveProof;

}
