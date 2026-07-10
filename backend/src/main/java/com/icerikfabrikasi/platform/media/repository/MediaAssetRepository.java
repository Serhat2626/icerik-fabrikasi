package com.icerikfabrikasi.platform.media.repository;

import com.icerikfabrikasi.platform.media.entity.AssetType;
import com.icerikfabrikasi.platform.media.entity.MediaAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * MediaAsset entity'si için repository.
 */
@Repository
public interface MediaAssetRepository extends JpaRepository<MediaAsset, UUID> {

    List<MediaAsset> findByAssetType(AssetType assetType);

    List<MediaAsset> findAllByOrderByCreatedAtDesc();
}
