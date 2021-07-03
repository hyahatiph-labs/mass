package com.hiahatf.mass.models;

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
public class MoneroValidateAddressResponse {
    private String id;
    private String jsonrpc;
    private MoneroValidateAddressResult result;
}
