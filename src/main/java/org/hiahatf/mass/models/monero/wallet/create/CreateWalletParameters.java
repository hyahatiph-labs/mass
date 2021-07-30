package org.hiahatf.mass.models.monero.wallet.create;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the create_wallet params.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateWalletParameters {
    private String filename;
    private final String language = "English";
}
