package org.hiahatf.mass.models.monero.validate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the validate_address params.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateAddressParameters {
    private String address;
    private final boolean any_net_type = true;
    private final boolean allow_openalias = true;
}
