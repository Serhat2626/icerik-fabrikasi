package com.icerikfabrikasi.platform.shorts.publish;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.channel.affiliate.AffiliateLink;
import com.icerikfabrikasi.platform.channel.affiliate.AffiliateLinkService;
import com.icerikfabrikasi.platform.shorts.ShortsScript;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * YouTube video açıklama oluşturucu.
 *
 * <p>Kanal bazlı, affiliate-aware video açıklamaları üretir.
 * Konu bazlı akıllı link eşleştirmesiyle en uygun affiliate
 * linklerini otomatik olarak açıklamaya ekler.</p>
 *
 * <p>Çıktı yapısı:
 * <pre>
 * {hook}
 *
 * 🔗 Tools & Links:
 * 📲 Try Wise FREE: https://...
 * 📲 Get Revolut: https://...
 *
 * ━━━━━━━━━━━━━━━━━━━━━━━━
 * 📱 Follow: @ChannelHandle
 *
 * #hashtag1 #hashtag2 ...
 *
 * ⚠️ Disclaimer text
 * </pre></p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VideoDescriptionBuilder {

    private final AffiliateLinkService affiliateLinkService;

    /** Maksimum açıklamada gösterilecek affiliate link sayısı */
    private static final int MAX_LINKS_IN_DESCRIPTION = 5;

    /**
     * Video açıklaması oluşturur.
     *
     * @param script Video scripti
     * @param channel Kanal konfigürasyonu
     * @return YouTube'a gönderilecek tam açıklama metni
     */
    public String buildDescription(ShortsScript script, Channel channel) {
        StringBuilder desc = new StringBuilder();

        // 1. Hook — ilk satır (YouTube arama için önemli)
        desc.append(script.getHook()).append("\n\n");

        // 2. Affiliate linkler — konu bazlı eşleştirme
        String scriptFullText = buildFullScriptText(script);
        List<AffiliateLink> links = affiliateLinkService.matchLinksForContent(
                channel.getId(), script.getTopic(), scriptFullText);

        if (!links.isEmpty()) {
            desc.append("🔗 Tools & Links Mentioned:\n");
            links.stream()
                    .limit(MAX_LINKS_IN_DESCRIPTION)
                    .forEach(link -> {
                        String emoji = link.getEmoji() != null ? link.getEmoji() : "🔗";
                        desc.append(emoji).append(" ")
                                .append(link.getDisplayText())
                                .append(": ")
                                .append(link.getAffiliateUrl())
                                .append("\n");
                    });
            desc.append("\n");
        }

        // 3. Ayırıcı çizgi
        desc.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");

        // 4. Kanal handle / watermark
        if (channel.getWatermarkText() != null && !channel.getWatermarkText().isBlank()) {
            desc.append("📱 Follow: ").append(channel.getWatermarkText()).append("\n");
        }

        // 5. Hashtags
        if (script.getHashtags() != null && !script.getHashtags().isBlank()) {
            desc.append("\n").append(script.getHashtags().replace(",", " ")).append("\n");
        }

        // 6. Disclaimer
        desc.append("\n");
        String disclaimer = getDisclaimer(channel);
        desc.append(disclaimer);

        // 7. Affiliate disclosure (varsa link)
        if (!links.isEmpty()) {
            desc.append("\n📌 Some links above are affiliate links. ");
            desc.append("I may earn a small commission at no extra cost to you.");
        }

        log.info("Video açıklaması oluşturuldu: scriptId={}, affiliateLinks={}",
                script.getId(), Math.min(links.size(), MAX_LINKS_IN_DESCRIPTION));

        return desc.toString();
    }

    /**
     * Kanal nişine göre uygun disclaimer metni döndürür.
     */
    private String getDisclaimer(Channel channel) {
        if (channel.getDescriptionTemplate() != null && !channel.getDescriptionTemplate().isBlank()) {
            return channel.getDescriptionTemplate();
        }

        return switch (channel.getNiche()) {
            case MONEY_MINDSET -> "⚠️ This video is for educational & entertainment purposes only. Not financial advice. Always do your own research.";
            case PSYCHOLOGY -> "⚠️ This content is for educational purposes only. It is not a substitute for professional medical or psychological advice.";
            case AI_TECH -> "⚠️ Tool features and pricing may change. Always verify on the official website.";
            case CYBERSECURITY -> "⚠️ For educational purposes only. Never use this knowledge for illegal activities.";
            default -> "⚠️ This video is for educational & entertainment purposes only.";
        };
    }

    /**
     * Script'in tüm text'ini birleştirir (affiliate link eşleştirme için).
     */
    private String buildFullScriptText(ShortsScript script) {
        StringBuilder sb = new StringBuilder();
        sb.append(script.getHook()).append(" ");
        if (script.getScenes() != null) {
            script.getScenes().forEach(scene -> {
                if (scene.getText() != null) sb.append(scene.getText()).append(" ");
                if (scene.getVisualHint() != null) sb.append(scene.getVisualHint()).append(" ");
            });
        }
        if (script.getCta() != null) sb.append(script.getCta());
        return sb.toString();
    }
}
