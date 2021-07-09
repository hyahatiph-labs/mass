package com.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero validate address request params
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroValidateAddressParameters {
    private String address;
    private final boolean any_net_type = true;
    private final boolean allow_openalias = true;
}
