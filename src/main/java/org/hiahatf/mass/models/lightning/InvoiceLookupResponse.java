package org.hiahatf.mass.models.lightning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Invoice Lookup Response.
 * See lightning API for more info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceLookupResponse {
    private InvoiceState state;
}
