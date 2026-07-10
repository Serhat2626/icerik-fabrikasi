package com.icerikfabrikasi.platform.shorts;

import com.icerikfabrikasi.platform.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Shorts video script'inin tek bir sahne öğesi.
 *
 * <p>Her sahne ekranda gösterilecek bir text overlay + arka plan görsel ipucu içerir.
 * Sahneler sıralı olarak video'da render edilir.</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "shorts_script_scenes")
public class ShortsScriptScene extends BaseEntity {

    /** Bu sahnenin ait olduğu script */
    @ManyToOne(fetch = FetchType.LAZY)
    private ShortsScript shortsScript;

    /** Sahne sıra numarası (0-based) */
    @Column(nullable = false)
    private int orderIndex;

    /** Ekranda gösterilecek metin (max 15 kelime) */
    @Column(nullable = false, length = 500)
    private String text;

    /** Görsel ipucu — hangi stock footage/AI görsel kullanılacak */
    @Column(length = 500)
    private String visualHint;

    /** Sahne süresi (saniye) — varsayılan ~5 sn */
    private int durationSeconds;
}
