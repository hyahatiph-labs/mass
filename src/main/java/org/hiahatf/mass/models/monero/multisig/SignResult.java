package org.hiahatf.mass.models.monero.multisig;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the sign_multisig result.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignResult {
    private String tx_data_hex;
    private List<String> tx_hash_list;
}
