package com.icerikfabrikasi.platform.channel;

/**
 * Kanal niş kategorisi.
 *
 * <p>Her YouTube kanalının hangi niş'te içerik ürettiğini belirler.
 * AI prompt template'leri ve görsel stil bu enum'a göre seçilir.</p>
 */
public enum ChannelNiche {

    /** Kişisel finans, para yönetimi, zenginlik alışkanlıkları */
    MONEY_MINDSET("Money Mindset", "Personal finance, money rules, wealth habits"),

    /** Genel kültür quiz, bilgi yarışması */
    QUIZ_TRIVIA("Quiz & Trivia", "General knowledge quiz, trivia challenges"),

    /** AI araç tanıtımı, teknoloji haberleri */
    AI_TECH("AI & Tech", "AI tools, tech news, future technology");

    private final String displayName;
    private final String description;

    ChannelNiche(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }
}
