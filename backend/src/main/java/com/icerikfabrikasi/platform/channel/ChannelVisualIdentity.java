package com.icerikfabrikasi.platform.channel;

import lombok.Builder;
import lombok.Value;

/**
 * Kanal görsel kimliği konfigürasyonu.
 *
 * <p>Her kanalın kendine özgü renk paleti, font seçimi ve
 * text overlay stili bu yapıda tutulur. Video render sırasında
 * FFmpeg bu değerleri kullanır.</p>
 */
@Value
@Builder
public class ChannelVisualIdentity {

    /** Ana arka plan rengi (hex) — örn: "#0D0D0D" */
    String backgroundColor;

    /** Birincil metin rengi (hex) — örn: "#FFFFFF" */
    String primaryTextColor;

    /** Vurgu rengi (hex) — hook ve önemli kelimeler için — örn: "#FFD700" */
    String accentColor;

    /** İkincil vurgu rengi (hex) — gradyan için — örn: "#FF6B35" */
    String secondaryAccentColor;

    /** Font ailesi — örn: "Montserrat", "Inter", "Bebas Neue" */
    String fontFamily;

    /** Hook text boyutu (px) */
    @Builder.Default
    int hookFontSize = 96;

    /** Gövde text boyutu (px) */
    @Builder.Default
    int bodyFontSize = 72;

    /** Text overlay animasyon stili */
    @Builder.Default
    TextAnimationStyle animationStyle = TextAnimationStyle.FADE_IN;

    /** Text gölge efekti */
    @Builder.Default
    boolean textShadowEnabled = true;

    /** Alt bar / watermark metni — örn: "@MoneyMindsetDaily" */
    String watermarkText;
}
