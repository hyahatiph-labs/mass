package org.hiahatf.mass.models.monero.transfer;

import java.util.List;

import org.hiahatf.mass.models.monero.Destination;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the transfer params.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferParameters {
    private List<Destination> destinations;
    private final int account_index = 0;
    private final int[] subaddr_indices = {0};
    private final int priority = 0;
    private final int ring_size = 11;
}
