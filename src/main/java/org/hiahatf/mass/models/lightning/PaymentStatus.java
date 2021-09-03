package org.hiahatf.mass.models.lightning;

/**
 * Payment status from the v2/router/send API call
 */
public enum PaymentStatus {
    UNKNOWN,
    IN_FLIGHT,
    SUCCEEDED,
    FAILED
}
