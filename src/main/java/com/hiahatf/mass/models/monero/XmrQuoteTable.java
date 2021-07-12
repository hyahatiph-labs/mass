package com.hiahatf.mass.models.monero;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table; 

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the Monero Quote DB persistance
 */
@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XmrQuoteTable {

    @Id
    private String quote_id;
    @Column
    private Double amount;
    @Column
    private byte[] preimage;
    @Column
    private byte[] payment_hash;
    @Column
    private String xmr_address;

}
