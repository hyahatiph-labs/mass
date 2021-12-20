package org.hiahatf.mass.models.bitcoin;

import java.util.List;

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
    // base32 peerId
    private String peerId;
    // address to use for reserve proof
    private String proofAddress;
    // clients's reserve proof
    private String proofSignature;
    // client's xmr refund address
    private String refundAddress;
    // client's prepare multisig info
    // (should be at least n=2 because client will need to create their own mediator)
    private List<String> swapMultisigInfos;
}
