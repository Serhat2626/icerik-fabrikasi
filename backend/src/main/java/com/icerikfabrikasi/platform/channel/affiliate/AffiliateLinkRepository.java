package com.icerikfabrikasi.platform.channel.affiliate;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Affiliate link veritabanı erişim katmanı.
 */
@Repository
public interface AffiliateLinkRepository extends JpaRepository<AffiliateLink, UUID> {

    /** Bir kanalın aktif affiliate linklerini öncelik sırasına göre getirir. */
    List<AffiliateLink> findByChannelIdAndActiveTrueOrderByPriorityAsc(UUID channelId);

    /** Bir kanalın tüm affiliate linklerini getirir. */
    List<AffiliateLink> findByChannelIdOrderByPriorityAsc(UUID channelId);

    /** Belirli bir ürün adına sahip aktif linkleri bulur. */
    List<AffiliateLink> findByChannelIdAndProductNameAndActiveTrue(UUID channelId, String productName);
}
