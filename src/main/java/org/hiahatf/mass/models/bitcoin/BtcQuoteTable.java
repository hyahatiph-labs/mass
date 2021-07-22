package org.hiahatf.mass.models.bitcoin;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table; 

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the Bitcoin Quote DB persistance.
 * The payment hash is extracted from the payment
 * request and saved as quote_id. The amount of
 * Monero is saved along with the Monero refund
 * address.
 */
@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BtcQuoteTable {

    @Id
    private String quote_id;
    @Column
    private Double amount;
    @Column
    private String payment_request;
    @Column
    private String refund_address;

}
