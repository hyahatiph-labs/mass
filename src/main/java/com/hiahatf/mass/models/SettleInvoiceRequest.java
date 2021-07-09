package com.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Settle Invoice Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SettleInvoiceRequest {
    private byte[] preimage;
}
