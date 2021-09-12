package org.hiahatf.mass.models.lightning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for the v2/router/send response.
 * See lightning API for more info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouterSendResponse {
    private PaymentStatus status;
}
