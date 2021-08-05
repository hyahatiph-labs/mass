package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Container for holding multisig info for the swap
 * and mediator wallets for future processing.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultisigData {
    private String clientMultisigInfo;
    private String mediatorFilename;
    private String mediatorMultisigInfo;
    private String swapAddress;
    private String swapFilename;
    private String swapMultisigInfo;
}
