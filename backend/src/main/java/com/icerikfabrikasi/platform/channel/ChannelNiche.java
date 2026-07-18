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
    AI_TECH("AI & Tech", "AI tools, tech news, future technology"),

    /** Psikoloji, insan davranışları, beyin bilimi */
    PSYCHOLOGY("Psychology", "Psychology, human behavior, brain science, cognitive biases"),

    /** Şaşırtıcı bilgiler, biliyor muydunuz içerikleri */
    FACTS_TRIVIA("Facts & Trivia", "Shocking facts, amazing knowledge, did-you-know content"),

    /** Hızlı tarifler, mutfak ipuçları */
    COOKING("Cooking", "Quick recipes, kitchen hacks, food content"),

    /** Şirket hikayeleri, iş dünyası belgeselleri */
    BUSINESS_DOCUMENTARY("Business Documentary", "Company stories, business history, entrepreneur profiles"),

    /** Siber güvenlik, dijital gizlilik */
    CYBERSECURITY("Cybersecurity", "Cyber security, hacking, digital privacy, VPN"),

    /** No-code araçlar, otomasyon, AI agent'lar */
    NOCODE_AUTOMATION("No-Code & Automation", "No-code tools, workflow automation, AI agents");

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
