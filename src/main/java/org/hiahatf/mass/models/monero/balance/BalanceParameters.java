package org.hiahatf.mass.models.monero.balance;

import lombok.Builder;
import lombok.Data;

/**
 * POJO for get_balance parameters.
 * See Monero RPC docs for more details.
 */
@Data
@Builder
public class BalanceParameters {
    private final int account_index = 0;
}
