package com.icerikfabrikasi.platform.shorts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * ShortsScript JPA repository.
 */
@Repository
public interface ShortsScriptRepository extends JpaRepository<ShortsScript, UUID> {

    List<ShortsScript> findByChannelIdOrderByCreatedAtDesc(UUID channelId);

    List<ShortsScript> findByChannelIdAndStatus(UUID channelId, ShortsScriptStatus status);

    long countByChannelId(UUID channelId);

    long countByChannelIdAndStatus(UUID channelId, ShortsScriptStatus status);
}
