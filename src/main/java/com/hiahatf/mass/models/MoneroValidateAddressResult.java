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
public class MoneroValidateAddressResult {
    private boolean valid;
    private boolean integrated;
    private boolean subaddress;
    private String nettype;
    private boolean openalias_address;
}
