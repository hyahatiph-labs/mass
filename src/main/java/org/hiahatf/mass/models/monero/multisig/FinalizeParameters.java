package org.hiahatf.mass.models.monero.multisig;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the finalize_multisig parameters.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeParameters {
    private List<String> multisig_info;
}
