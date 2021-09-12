package org.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Integrity Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Integrity {
    private String integrity;
    private String btcQuoteController;
    private String btcSwapController;
    private String btcQuoteService;
    private String btcSwapService;
    private String xmrQuoteController;
    private String xmrSwapController;
    private String xmrQuoteService;
    private String xmrSwapService;
}
