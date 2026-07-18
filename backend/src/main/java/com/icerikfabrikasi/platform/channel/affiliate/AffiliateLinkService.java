package com.icerikfabrikasi.platform.channel.affiliate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Affiliate link yönetim servisi.
 *
 * <p>Kanal bazlı affiliate link CRUD işlemleri ve
 * konu bazlı akıllı link eşleştirmesi yapar.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AffiliateLinkService {

    private final AffiliateLinkRepository repository;

    /**
     * Kanala yeni affiliate link ekler.
     */
    @Transactional
    public AffiliateLink addLink(UUID channelId, String productName, String affiliateUrl,
                                  CommissionType commissionType, String commissionValue,
                                  String displayText, String keywords, int priority, String emoji) {
        // Channel lazy-load için proxy
        AffiliateLink link = AffiliateLink.builder()
                .channel(com.icerikfabrikasi.platform.channel.Channel.builder().build()) // will be set via channelId
                .productName(productName)
                .affiliateUrl(affiliateUrl)
                .commissionType(commissionType)
                .commissionValue(commissionValue)
                .displayText(displayText)
                .keywords(keywords)
                .priority(priority)
                .emoji(emoji != null ? emoji : "🔗")
                .active(true)
                .build();
        return repository.save(link);
    }

    /**
     * Bir kanalın tüm aktif affiliate linklerini getirir.
     */
    public List<AffiliateLink> getActiveLinks(UUID channelId) {
        return repository.findByChannelIdAndActiveTrueOrderByPriorityAsc(channelId);
    }

    /**
     * Bir kanalın tüm affiliate linklerini getirir.
     */
    public List<AffiliateLink> getAllLinks(UUID channelId) {
        return repository.findByChannelIdOrderByPriorityAsc(channelId);
    }

    /**
     * Video konusu ve script metnine göre en uygun affiliate linkleri seçer.
     *
     * <p>Eşleştirme mantığı: link'in keywords alanındaki kelimeler
     * topic veya scriptText içinde geçiyorsa eşleşir.
     * Eşleşmeyen linkler de eklenir ama sona konur.</p>
     *
     * @param channelId Kanal ID
     * @param topic Video konusu
     * @param scriptText Tüm script metni (hook + sahneler + CTA)
     * @return Öncelik sırasına göre sıralanmış linkler (eşleşenler önce)
     */
    public List<AffiliateLink> matchLinksForContent(UUID channelId, String topic, String scriptText) {
        List<AffiliateLink> allLinks = getActiveLinks(channelId);
        if (allLinks.isEmpty()) return allLinks;

        String searchText = ((topic != null ? topic : "") + " " + (scriptText != null ? scriptText : "")).toLowerCase();

        List<AffiliateLink> matched = new ArrayList<>();
        List<AffiliateLink> unmatched = new ArrayList<>();

        for (AffiliateLink link : allLinks) {
            if (link.getKeywords() == null || link.getKeywords().isBlank()) {
                // Keyword'suz linkler her zaman dahil (genel linkler)
                matched.add(link);
                continue;
            }

            String[] keywords = link.getKeywords().split(",");
            boolean found = Arrays.stream(keywords)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .anyMatch(searchText::contains);

            if (found) {
                matched.add(link);
            } else {
                unmatched.add(link);
            }
        }

        // Eşleşenler önce, sonra eşleşmeyenler
        matched.addAll(unmatched);

        log.info("Affiliate link eşleştirme: channelId={}, topic='{}', eşleşen={}, toplam={}",
                channelId, topic, matched.size() - unmatched.size(), allLinks.size());

        return matched;
    }

    /**
     * Linki devre dışı bırakır.
     */
    @Transactional
    public void deactivateLink(UUID linkId) {
        repository.findById(linkId).ifPresent(link -> {
            link.setActive(false);
            repository.save(link);
            log.info("Affiliate link devre dışı: id={}, product={}", linkId, link.getProductName());
        });
    }

    /**
     * Linki günceller.
     */
    @Transactional
    public AffiliateLink updateLink(UUID linkId, String affiliateUrl, String displayText, 
                                     String keywords, int priority, boolean active) {
        AffiliateLink link = repository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Affiliate link bulunamadı: " + linkId));
        if (affiliateUrl != null) link.setAffiliateUrl(affiliateUrl);
        if (displayText != null) link.setDisplayText(displayText);
        if (keywords != null) link.setKeywords(keywords);
        link.setPriority(priority);
        link.setActive(active);
        return repository.save(link);
    }
}
