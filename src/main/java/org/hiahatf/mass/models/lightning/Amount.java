package org.hiahatf.mass.models.lightning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the balance amount.
 * Matches with the LND lnrpcAmount model.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Amount {
    private String sat;
    private String msat;
}
