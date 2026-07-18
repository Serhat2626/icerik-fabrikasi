package com.icerikfabrikasi.platform.channel.affiliate;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * Kanal affiliate link entity'si.
 *
 * <p>Her kanal birden fazla affiliate link taşıyabilir.
 * Video yayınlarken bu linkler otomatik olarak açıklamaya eklenir.</p>
 *
 * <p>Konu bazlı eşleştirme: keywords alanı video topic'i ile karşılaştırılarak
 * en uygun linkler seçilir.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "affiliate_links")
public class AffiliateLink extends BaseEntity {

    /** Bu linkin ait olduğu kanal */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_id", nullable = false)
    private Channel channel;

    /** Ürün/servis adı — örn: "Wise", "Hostinger", "BetterHelp" */
    @Column(nullable = false, length = 100)
    private String productName;

    /** Affiliate tracking URL'i */
    @Column(nullable = false, length = 500)
    private String affiliateUrl;

    /** Komisyon türü */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommissionType commissionType;

    /** Komisyon değeri açıklaması — örn: "60%", "$30-100", "€10-25" */
    @Column(length = 50)
    private String commissionValue;

    /** Video açıklamasında gösterilecek metin — örn: "📲 Try Wise FREE" */
    @Column(nullable = false, length = 200)
    private String displayText;

    /** Konu eşleştirme anahtar kelimeleri (virgülle ayrılmış) */
    @Column(length = 500)
    private String keywords;

    /** Açıklamadaki sıralama önceliği (1 = en üstte) */
    @Builder.Default
    private int priority = 5;

    /** Link aktif mi */
    @Builder.Default
    private boolean active = true;

    /** Emoji ikonu — örn: "📲", "🔗", "💰" */
    @Column(length = 10)
    @Builder.Default
    private String emoji = "🔗";
}
