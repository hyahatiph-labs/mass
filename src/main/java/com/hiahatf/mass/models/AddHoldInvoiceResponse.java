package com.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the add hold invoice response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddHoldInvoiceResponse {
    private String payment_request;
}
