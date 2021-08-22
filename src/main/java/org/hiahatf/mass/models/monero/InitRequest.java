package org.hiahatf.mass.models.monero;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for extracting export_multisig_info from
 * client and calling with import multisig info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitRequest {
    private String hash;
    private String importInfo;
}
