package org.hiahatf.mass.models.peer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for the Peer DB persistance
 */
@Entity
@Table
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Peer {
    
    @Id // base32 peer id
    private String quote_id;
    @Column // peer was online in the past 3600s
    private boolean isActive;
    @Column // peer has good reputation (e.g. multiple successful swaps)
    private boolean isVetted;
    @Column // peer has bad reputation (e.g. multiple successive swap cancels)
    private boolean isMalicous;
}
