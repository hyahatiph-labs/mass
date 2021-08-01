package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero quote request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    // valid monero address
    private String address;
    // amount in monero
    private double amount;
    // clients' prepare multisig info
    private String multisigInfo;
}
