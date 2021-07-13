package org.hiahatf.mass.models.lightning;

/**
 * Enum for the invoice state. Mass creates OPEN invoices
 * and expects an ACCEPTED state when swapping. 
 * Failure to complete a swap sends it to CANCELLED.
 * Successful swaps are put to SETTLED state.
 */
public enum InvoiceState {
    ACCEPTED,
    CANCELLED,
    OPEN,
    SETTLED
}
