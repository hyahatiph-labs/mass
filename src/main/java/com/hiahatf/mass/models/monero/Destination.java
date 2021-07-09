package com.hiahatf.mass.models.monero;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero transfer destination
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Destination {
    private Long amount;
    private String address;
}
