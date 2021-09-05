package org.hiahatf.mass.models.monero;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the Monero Quote DB persistance.
 * The payment hash is duplicated due to some
 * conversion with hibernate. The String value
 * is needed for DB query. The byte[] is needed
 * to cancel an invoice.
 */
@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmrQuoteTable {

    @Id // preimage hash from client
    private String quote_id;
    @Column // amount of monero requested
    private Double amount;
    @Column // final stop for the swap
    private String dest_address;
    @Column // funding transaction id
    private String funding_txid;
    @Column // mediators' filename for wallet control
    private String mediator_filename;
    @Column // mediators' info for finalizing the consensus wallet
    private String mediator_finalize_msig;
    @Column // store the preimage hash as byte array
    private byte[] payment_hash;
    @Column // multisig address generated for participants
    private String swap_address;
    @Column // filename used for wallet control
    private String swap_filename;
    @Column // swap info for finalizing the consensus wallet
    private String swap_finalize_msig;
    
}
