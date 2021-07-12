package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero transfer result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroTransferResult {
    private long amount;
    private long fee;
    private String multisig_txset;
    private String tx_blob;
    private String tx_hash;
    private String tx_key;
    private String tx_metadata;
    private String unsigned_txset;
}
