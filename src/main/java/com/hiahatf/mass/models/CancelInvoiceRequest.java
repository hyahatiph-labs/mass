package com.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the cancel hold invoice request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelInvoiceRequest {
    byte[] payment_hash;   
}
