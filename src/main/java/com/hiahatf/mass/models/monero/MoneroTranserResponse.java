package com.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero transfer response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroTranserResponse {
    private String id;
    private String jsonrpc;
    private MoneroTransferResult result;
}