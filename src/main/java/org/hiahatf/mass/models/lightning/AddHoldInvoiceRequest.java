package org.hiahatf.mass.models.lightning;

import org.hiahatf.mass.models.Constants;

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
    // memo for the invoice
    private final String memo = Constants.MEMO;
    // preimage hash
    private byte[] hash;
    // invoice amount in satoshis
    private String value;
    // invoices expire after 30 min.
    private final String expiry = Constants.EXPIRY;
}
