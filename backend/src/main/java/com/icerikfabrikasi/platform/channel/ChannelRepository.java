package com.icerikfabrikasi.platform.channel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Channel JPA repository.
 */
@Repository
public interface ChannelRepository extends JpaRepository<Channel, UUID> {

    List<Channel> findByStatus(ChannelStatus status);

    List<Channel> findByNiche(ChannelNiche niche);

    Optional<Channel> findByNameIgnoreCase(String name);
}
