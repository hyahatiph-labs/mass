package org.hiahatf.mass.models.monero;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the describe_transfer description
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Description {
    private long amount_in;
    private long amount_out;
    private String change_address;
    private long change_amount;
    private int dummy_outputs;
    private long fee;
    private List<Destination> recipients;
    private int ring_size;
    private long unlock_time;
} 
