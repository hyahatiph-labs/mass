package com.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero validate address request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroValidateAddressRequest {
    private final String jsonrpc = "2.0";
    private final String id = "0";
    private final String method = "validate_address";
    private MoneroValidateAddressParameters params;
}