package com.icerikfabrikasi.platform.channel;

import com.icerikfabrikasi.platform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * YouTube / sosyal medya kanal entity'si.
 *
 * <p>Her kanal bir niş'e sahiptir ve kendine özgü görsel kimlik,
 * AI prompt konfigürasyonu ve yayın takvimi taşır.</p>
 *
 * <p>Tüm kanallar İçerik Fabrikası platformu üzerinden yönetilir.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "channels")
public class Channel extends BaseEntity {

    /** Kanal adı — örn: "Money Mindset Daily" */
    @Column(nullable = false, length = 100)
    private String name;

    /** Kanal açıklaması */
    @Column(length = 500)
    private String description;

    /** Kanal niş kategorisi */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ChannelNiche niche;

    /** Hedef platform */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TargetPlatform targetPlatform;

    /** Kanal durumu */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChannelStatus status;

    /** İçerik dili — örn: "en", "tr" */
    @Column(nullable = false, length = 5)
    private String contentLanguage;

    /** Haftalık hedef video sayısı */
    private int weeklyVideoTarget;

    // ── Görsel kimlik ──

    /** Ana arka plan rengi (hex) */
    @Column(length = 10)
    private String backgroundColor;

    /** Birincil metin rengi (hex) */
    @Column(length = 10)
    private String primaryTextColor;

    /** Vurgu rengi (hex) */
    @Column(length = 10)
    private String accentColor;

    /** İkincil vurgu rengi (hex) */
    @Column(length = 10)
    private String secondaryAccentColor;

    /** Font ailesi */
    @Column(length = 50)
    private String fontFamily;

    /** Text animasyon stili */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private TextAnimationStyle animationStyle;

    /** Watermark / kanal handle — örn: "@MoneyMindsetDaily" */
    @Column(length = 50)
    private String watermarkText;

    // ── AI prompt konfigürasyonu ──

    /** AI sistem prompt'u */
    @Column(columnDefinition = "TEXT")
    private String aiSystemPrompt;

    /** Script üretim template'i */
    @Column(columnDefinition = "TEXT")
    private String aiScriptTemplate;

    /** İçerik tonu — örn: "authoritative, direct" */
    @Column(length = 200)
    private String contentTone;

    /** Hedef video süresi (saniye) */
    private int targetDurationSeconds;

    /** Hedef sahne sayısı */
    private int targetSceneCount;

    // ── Metrikler ──

    /** Üretilen toplam video sayısı */
    private int totalVideosProduced;

    /** Yayınlanan toplam video sayısı */
    private int totalVideosPublished;

    /**
     * Bu kanalın görsel kimlik konfigürasyonunu oluşturur.
     */
    public ChannelVisualIdentity toVisualIdentity() {
        return ChannelVisualIdentity.builder()
                .backgroundColor(backgroundColor)
                .primaryTextColor(primaryTextColor)
                .accentColor(accentColor)
                .secondaryAccentColor(secondaryAccentColor)
                .fontFamily(fontFamily)
                .animationStyle(animationStyle != null ? animationStyle : TextAnimationStyle.FADE_IN)
                .watermarkText(watermarkText)
                .build();
    }

    /**
     * Bu kanalın AI prompt konfigürasyonunu oluşturur.
     */
    public ChannelPromptConfig toPromptConfig() {
        return ChannelPromptConfig.builder()
                .systemPrompt(aiSystemPrompt)
                .scriptTemplate(aiScriptTemplate)
                .contentTone(contentTone)
                .targetDurationSeconds(targetDurationSeconds)
                .targetSceneCount(targetSceneCount)
                .build();
    }

    /** Video üretim sayacını artır */
    public void incrementVideosProduced() {
        this.totalVideosProduced++;
    }

    /** Video yayınlama sayacını artır */
    public void incrementVideosPublished() {
        this.totalVideosPublished++;
    }
}
