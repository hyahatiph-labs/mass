package org.hiahatf.mass.models.monero.validate;

import lombok.Builder;
import lombok.Data;

/**
 * POJO for the get_address params.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
public class GetAddressParameters {
    private final int account_index = 0;   
}
