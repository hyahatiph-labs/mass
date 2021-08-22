package org.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for response of initialization of swap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InitResponse {
    private String hash;
    private String swapExportInfo;
    private String mediatorExportSwapInfo;
}
