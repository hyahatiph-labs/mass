package org.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Rate Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rate {
    private Double rate;
}
