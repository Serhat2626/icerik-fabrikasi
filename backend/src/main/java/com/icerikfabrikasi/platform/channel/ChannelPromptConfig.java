package com.icerikfabrikasi.platform.channel;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * AI script üretimi için prompt konfigürasyonu.
 *
 * <p>Her kanalın kendine özgü prompt template'i, ton, kısıtlamalar
 * ve hook formatları bu yapıda tutulur.</p>
 */
@Value
@Builder
public class ChannelPromptConfig {

    /** Sistem prompt'u — AI'a kanalın kimliğini tanımlar */
    String systemPrompt;

    /** Script üretim prompt template'i — {topic} placeholder kullanır */
    String scriptTemplate;

    /** İçerik tonu — örn: "authoritative, direct, slightly provocative" */
    String contentTone;

    /** Hedef video süresi (saniye) */
    @Builder.Default
    int targetDurationSeconds = 45;

    /** Sahne sayısı (text overlay sayısı) */
    @Builder.Default
    int targetSceneCount = 8;

    /** Hook formatları — AI'ın kullanabileceği hook template'leri */
    List<String> hookTemplates;

    /** Yasaklı kelimeler — bu kelimeler script'te kullanılmaz */
    List<String> bannedWords;

    /** CTA (Call to Action) template'leri */
    List<String> ctaTemplates;
}
