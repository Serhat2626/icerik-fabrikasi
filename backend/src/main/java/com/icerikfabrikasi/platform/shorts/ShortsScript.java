package com.icerikfabrikasi.platform.shorts;

import com.icerikfabrikasi.platform.channel.Channel;
import com.icerikfabrikasi.platform.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * YouTube Shorts video script'i.
 *
 * <p>Bir Shorts video'su için gereken tüm metin içeriğini barındırır:
 * başlık, hook, sahneler (text overlay'ler) ve CTA.</p>
 *
 * <p>Lifecycle: DRAFT → IN_REVIEW → APPROVED → RENDERED → PUBLISHED</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shorts_scripts")
public class ShortsScript extends BaseEntity {

    /** Bu script'in ait olduğu kanal */
    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;

    /** Video başlığı (YouTube title) — max 60 karakter, emoji içerebilir */
    @Column(nullable = false, length = 200)
    private String title;

    /** Konu/topic — AI'a verilen orijinal konu */
    @Column(nullable = false, length = 500)
    private String topic;

    /** Hook — ilk 3 saniyede gösterilecek provokatif soru veya ifade */
    @Column(nullable = false, length = 500)
    private String hook;

    /** Kapanış CTA (Call to Action) */
    @Column(length = 300)
    private String cta;

    /** Hashtag'ler (virgülle ayrılmış) — örn: "#money,#finance,#wealth" */
    @Column(length = 500)
    private String hashtags;

    /** Script durumu */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ShortsScriptStatus status;

    /** Hedef video süresi (saniye) */
    private int targetDurationSeconds;

    /** Sahneler (text overlay'ler) */
    @OneToMany(mappedBy = "shortsScript", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<ShortsScriptScene> scenes = new ArrayList<>();

    /** Review notu — admin'in bıraktığı not */
    @Column(columnDefinition = "TEXT")
    private String reviewNotes;

    /** Onaylanma zamanı */
    private LocalDateTime approvedAt;

    /** Yayınlanma zamanı */
    private LocalDateTime publishedAt;

    /** Video render output path (render sonrası) */
    @Column(length = 500)
    private String renderOutputPath;

    /**
     * Script'e yeni sahne ekler.
     */
    public void addScene(String text, String visualHint, int durationSeconds) {
        ShortsScriptScene scene = ShortsScriptScene.builder()
                .shortsScript(this)
                .orderIndex(scenes.size())
                .text(text)
                .visualHint(visualHint)
                .durationSeconds(durationSeconds)
                .build();
        scenes.add(scene);
    }

    /**
     * Toplam tahmini süreyi hesaplar.
     */
    public int calculateTotalDuration() {
        return scenes.stream()
                .mapToInt(ShortsScriptScene::getDurationSeconds)
                .sum();
    }

    /**
     * Script'i onaylar.
     */
    public void approve() {
        this.status = ShortsScriptStatus.APPROVED;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Script'i yayınlandı olarak işaretler.
     */
    public void markPublished() {
        this.status = ShortsScriptStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
    }
}
