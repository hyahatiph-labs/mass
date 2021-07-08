package com.hiahatf.mass.models;

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
    private String xmr_address;
    @Column
    private byte[] preimage;
    @Column
    private byte[] preimage_hash;
    @Column
    private Boolean fulfilled;

}
