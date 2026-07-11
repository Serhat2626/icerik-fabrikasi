package com.icerikfabrikasi.platform.shorts.media;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Sahne metni ve görsel ipucundan AI görsel üretimi için
 * yüksek kaliteli prompt oluşturur.
 *
 * <p>Shorts video'ya uygun: dikey, sinematik, ilgi çekici görseller.</p>
 */
@Component
@Slf4j
public class AiScenePromptBuilder {

    /**
     * Sahne için AI görsel prompt'u oluşturur.
     *
     * @param sceneText Sahne metni (örn: "Rule 1: Pay yourself first")
     * @param visualHint Görsel ipucu (örn: "money jar savings")
     * @param isHook Hook sahnesi mi?
     * @return Detaylı İngilizce görsel prompt
     */
    public String buildPrompt(String sceneText, String visualHint, boolean isHook) {
        String baseContext = extractVisualContext(sceneText, visualHint);

        if (isHook) {
            return String.format(
                    "Dramatic cinematic vertical photo, %s, " +
                    "dark moody dramatic lighting with golden highlights, " +
                    "ultra sharp focus, shallow depth of field, " +
                    "professional photography, 9:16 portrait orientation, " +
                    "bold and eye-catching composition, Instagram viral aesthetic, " +
                    "no text no letters no words no watermark",
                    baseContext
            );
        }

        return String.format(
                "Cinematic vertical photo, %s, " +
                "professional studio lighting, rich colors, " +
                "sharp focus, beautiful bokeh background, " +
                "high-end photography style, 9:16 portrait orientation, " +
                "engaging and visually stunning composition, " +
                "no text no letters no words no watermark",
                baseContext
        );
    }

    /**
     * Sahne metni ve visualHint'ten görsel bağlamı çıkarır.
     */
    private String extractVisualContext(String sceneText, String visualHint) {
        // visualHint varsa onu öncelikli kullan
        if (visualHint != null && !visualHint.isBlank()) {
            return visualHint;
        }

        // Text'ten anahtar kelimelere göre görsel bağlam üret
        if (sceneText == null) return "abstract dark background";

        String lower = sceneText.toLowerCase();

        // Finans / para temaları
        if (lower.contains("money") || lower.contains("rich") || lower.contains("wealth")) {
            return "luxury gold coins and cash money on dark velvet surface";
        }
        if (lower.contains("invest") || lower.contains("stock") || lower.contains("market")) {
            return "glowing stock market chart going up on dark screen";
        }
        if (lower.contains("save") || lower.contains("saving")) {
            return "glass jar full of golden coins with growing plant";
        }
        if (lower.contains("debt") || lower.contains("liability")) {
            return "broken credit card on dark surface dramatic lighting";
        }
        if (lower.contains("income") || lower.contains("stream")) {
            return "multiple streams of water flowing into golden pool";
        }
        if (lower.contains("budget") || lower.contains("spend")) {
            return "neat organized wallet with cash and calculator";
        }

        // Motivasyon temaları
        if (lower.contains("start") || lower.contains("begin")) {
            return "person standing at sunrise on mountain top silhouette";
        }
        if (lower.contains("follow") || lower.contains("crowd")) {
            return "one person walking different path from crowd silhouette";
        }
        if (lower.contains("rule") || lower.contains("law")) {
            return "elegant golden rules text carved on dark marble";
        }
        if (lower.contains("wait") || lower.contains("hesitat")) {
            return "person standing at crossroads with dramatic fog";
        }

        // Teknoloji temaları
        if (lower.contains("ai") || lower.contains("tech") || lower.contains("robot")) {
            return "futuristic AI robot with glowing blue circuits";
        }
        if (lower.contains("future") || lower.contains("innovat")) {
            return "futuristic city skyline at night with neon lights";
        }

        // Genel
        if (lower.contains("comment") || lower.contains("follow") || lower.contains("subscribe")) {
            return "glowing social media engagement icons floating in dark space";
        }

        // Fallback
        return "abstract dark cinematic background with bokeh lights";
    }
}
