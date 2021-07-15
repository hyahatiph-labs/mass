package org.hiahatf.mass.models.lightning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for inbound / outbound liquidity.
 * This is used to hold the response from LND
 * pertaining to 'remote_balance' and 'local_balance'. 
 * The sum of those balances should suffice for verification
 * of ability to receive or send a payment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Liquidity {
    private Amount local_balance;
    private Amount remote_balance;
}
