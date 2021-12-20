package org.hiahatf.mass.repo;

import org.hiahatf.mass.models.Constants;
import org.hiahatf.mass.models.peer.Peer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Interface for performing database operations on the peer table
 */
@Repository(Constants.PEER_REPO)
public interface PeerRepository extends CrudRepository<Peer, String>
{
    
}
