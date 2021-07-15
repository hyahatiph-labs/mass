package org.hiahatf.mass.models.monero.validate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the validate_address result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidateAddressResult {
    private boolean valid;
    private boolean integrated;
    private boolean subaddress;
    private String nettype;
    private boolean openalias_address;
}
