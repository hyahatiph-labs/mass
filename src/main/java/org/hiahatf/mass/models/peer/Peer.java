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
    private String peer_id;
    @Column // consecutive cancel counter
    private int cancel_counter;
    @Column // peer was online in the past 7200s
    private boolean active;
    @Column // peer has good reputation (e.g. 3 consecutive successful swaps)
    private boolean vetted;
    @Column // peer has bad reputation (e.g. 3 consecutive swap cancels)
    private boolean malicous;
    @Column // consecutive swap counter
    private int swap_counter;
    
}
