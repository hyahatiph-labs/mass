package org.hiahatf.mass.models.monero.multisig;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the make_multisig parameters.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MakeParameters {
    List<String> multisig_info;
    // mass is always using 2/3s multisig
    private final int threshold = 2;
}
