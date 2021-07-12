package org.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the add hold invoice request.
 * See lightning API for more info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddHoldInvoiceRequest {
    private final String memo = "mass";
    private byte[] hash;
    private String value;
    // invoices expire after 10 min.
    // TODO: make this configurable?
    private final String expiry = "600";
}
