package org.hiahatf.mass.models.monero.transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the transfer result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResult {
    private long amount;
    private long fee;
    private String multisig_txset;
    private String tx_blob;
    private String tx_hash;
    private String tx_key;
    private String tx_metadata;
    private String unsigned_txset;
}
