package com.hiahatf.mass.models;

/**
 * Enum for the invoice state
 * Mass creates an OPEN invoices
 * and expects and ACCEPTED state
 * when swapping. Failure to complete
 * a swap sends it to CANCELLED.
 */
public enum InvoiceState {
    ACCEPTED,
    CANCELLED,
    OPEN,
    SETTLED
}
