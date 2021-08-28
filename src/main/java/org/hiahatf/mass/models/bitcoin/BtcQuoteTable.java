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
    private String refund_address;
    @Column // funding transaction id
    private String funding_txid;
    @Column // mediators' filename for wallet control
    private String mediator_filename;
    @Column // mediators' info for finalizing the consensus wallet
    private String mediator_finalize_msig;
    @Column // store the preimage as byte array
    private byte[] preimage;
    @Column // store the preimage hash as byte array
    private byte[] payment_hash;
    @Column // multisig address generated for participants
    private String swap_address;
    @Column // filename used for wallet control
    private String swap_filename;
    @Column // swap info for finalizing the consensus wallet
    private String swap_finalize_msig;

}
