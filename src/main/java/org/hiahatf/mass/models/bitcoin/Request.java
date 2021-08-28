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
    // client's amount of monero (must be same as reserve proof)
    private Double amount;
    // address to use for reserve proof
    private String proofAddress;
    // client's prepare multisig info
    private String swapMultisigInfo;
    // mediator's prepare multisig info
    private String mediatorMultisigInfo;
    // client's xmr refund address
    private String refundAddress;
    // clients's reserve proof
    private String reserveProof;
}
