package com.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Swap Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapResponse {
    private String hash;
    private String txId;
}
