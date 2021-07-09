package com.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Swap Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SwapRequest {
    private String hash;
}
