package org.hiahatf.mass.models.monero.multisig;

import java.util.List;

import org.hiahatf.mass.models.monero.Description;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the describe_transfer response.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DescribeResult {
    private List<Description> desc;
}
