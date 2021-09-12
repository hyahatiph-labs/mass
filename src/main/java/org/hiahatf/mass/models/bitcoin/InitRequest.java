package org.hiahatf.mass.models.bitcoin;

import java.util.List;

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
    private List<String> importInfos;
    private String paymentRequest;
}