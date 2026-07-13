package com.icerikfabrikasi.platform.shorts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ShortsScript JPA repository.
 */
@Repository
public interface ShortsScriptRepository extends JpaRepository<ShortsScript, UUID> {

    @Query("SELECT s FROM ShortsScript s LEFT JOIN FETCH s.scenes WHERE s.id = :id")
    Optional<ShortsScript> findByIdWithScenes(@Param("id") UUID id);

    List<ShortsScript> findByChannelIdOrderByCreatedAtDesc(UUID channelId);

    List<ShortsScript> findByChannelIdAndStatus(UUID channelId, ShortsScriptStatus status);

    long countByChannelId(UUID channelId);

    long countByChannelIdAndStatus(UUID channelId, ShortsScriptStatus status);
}
