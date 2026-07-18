package com.icerikfabrikasi.platform.shorts.media;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sahne metni ve görsel ipucundan AI görsel üretimi için
 * yüksek kaliteli, sahneye özel prompt oluşturur.
 *
 * <p>Strateji: Sahne metnini doğrudan prompt'a dahil et,
 * böylece Flux Pro o cümleyi görsel olarak anlatır.</p>
 */
@Component
@Slf4j
public class AiScenePromptBuilder {

    /**
     * Sahne için AI görsel prompt'u oluşturur.
     * Sahne metnini doğrudan görsel açıklamaya dönüştürür.
     */
    public String buildPrompt(String sceneText, String visualHint, boolean isHook) {
        // visualHint varsa ve detaylıysa onu kullan, yoksa text'ten üret
        String subject = buildSubject(sceneText, visualHint);

        if (isHook) {
            return String.format(
                    "A stunning photorealistic image illustrating the concept: \"%s\". " +
                    "Dramatic cinematic lighting, dark moody atmosphere with golden warm highlights. " +
                    "Ultra detailed, 8K quality, shallow depth of field, " +
                    "professional commercial photography style. " +
                    "Vertical 9:16 portrait orientation. " +
                    "No text, no letters, no words, no watermark, no UI elements.",
                    subject
            );
        }

        return String.format(
                "A beautiful photorealistic image showing: \"%s\". " +
                "Clean professional lighting, rich vivid colors, sharp focus. " +
                "High-end editorial photography style, visually engaging composition. " +
                "Vertical 9:16 portrait orientation. " +
                "No text, no letters, no words, no watermark, no UI elements.",
                subject
        );
    }

    /**
     * Sahne için AI görsel prompt'u oluşturur — kanal bazlı stil preset'i ile.
     *
     * @param sceneText Sahne metni
     * @param visualHint Görsel ipucu
     * @param isHook Hook sahnesi mi
     * @param imageStylePreset Kanal bazlı görsel stil preset'i
     */
    public String buildPrompt(String sceneText, String visualHint, boolean isHook, String imageStylePreset) {
        String basePrompt = buildPrompt(sceneText, visualHint, isHook);
        if (imageStylePreset != null && !imageStylePreset.isBlank()) {
            String noTextDirective = "No text, no letters, no words, no watermark, no UI elements.";
            if (basePrompt.contains(noTextDirective)) {
                basePrompt = basePrompt.replace(noTextDirective,
                        "Style: " + imageStylePreset + ". " + noTextDirective);
            } else {
                basePrompt += " Style: " + imageStylePreset + ".";
            }
        }
        return basePrompt;
    }

    /**
     * Scene text ve visual hint'ten en iyi görsel açıklamayı seç.
     */
    private String buildSubject(String sceneText, String visualHint) {
        // visualHint detaylıysa (3+ kelime) onu kullan
        if (visualHint != null && !visualHint.isBlank() && visualHint.split("\\s+").length >= 3) {
            return visualHint;
        }

        // Scene text'ten doğrudan görsel konsept üret
        if (sceneText == null || sceneText.isBlank()) {
            return "abstract luxury dark background with golden light rays";
        }

        // Temizle — emojiler, özel karakterler kaldır
        String clean = sceneText
                .replaceAll("[^a-zA-Z0-9\\s.,!?'-]", "")
                .trim();

        // Kısa text ise doğrudan kullan
        if (clean.length() < 80) {
            return clean;
        }

        // Uzun text ise ilk cümleyi al
        int dotIdx = clean.indexOf('.');
        if (dotIdx > 10 && dotIdx < 80) {
            return clean.substring(0, dotIdx);
        }

        return clean.substring(0, Math.min(80, clean.length()));
    }
}
