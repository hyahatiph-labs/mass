package org.hiahatf.mass.models.monero.validate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the is_multisig result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IsMultisigResult {
    private boolean multisig;
    private boolean ready;
    private int threshold;
    private int total;   
}
