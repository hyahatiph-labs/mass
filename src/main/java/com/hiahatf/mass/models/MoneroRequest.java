package com.hiahatf.mass.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the Monero request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoneroRequest {
    private String address;
    private double amount;
}
