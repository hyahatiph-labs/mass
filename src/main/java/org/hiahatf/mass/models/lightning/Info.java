package org.hiahatf.mass.models.lightning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * POJO for LND info response.
 * Returns version on health check for nodes
 * to run compatibility checks.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Info {
    private String version;
}
