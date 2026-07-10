-- V3: Kanal yönetimi tablosu
-- İçerik Fabrikası platformunda yönetilen YouTube / sosyal medya kanalları

CREATE TABLE channels (
    id                       UUID PRIMARY KEY,
    name                     VARCHAR(100) NOT NULL,
    description              VARCHAR(500),
    niche                    VARCHAR(30) NOT NULL,
    target_platform          VARCHAR(30) NOT NULL DEFAULT 'YOUTUBE_SHORTS',
    status                   VARCHAR(20) NOT NULL DEFAULT 'SETUP',
    content_language         VARCHAR(5) NOT NULL DEFAULT 'en',
    weekly_video_target      INTEGER NOT NULL DEFAULT 5,

    -- Görsel kimlik
    background_color         VARCHAR(10),
    primary_text_color       VARCHAR(10),
    accent_color             VARCHAR(10),
    secondary_accent_color   VARCHAR(10),
    font_family              VARCHAR(50),
    animation_style          VARCHAR(20) DEFAULT 'FADE_IN',
    watermark_text           VARCHAR(50),

    -- AI prompt konfigürasyonu
    ai_system_prompt         TEXT,
    ai_script_template       TEXT,
    content_tone             VARCHAR(200),
    target_duration_seconds  INTEGER NOT NULL DEFAULT 45,
    target_scene_count       INTEGER NOT NULL DEFAULT 8,

    -- Metrikler
    total_videos_produced    INTEGER NOT NULL DEFAULT 0,
    total_videos_published   INTEGER NOT NULL DEFAULT 0,

    -- Zaman damgaları
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP
);

CREATE INDEX idx_channels_status ON channels(status);
CREATE INDEX idx_channels_niche ON channels(niche);

-- ═══════════════════════════════════════════════════
-- SEED DATA: Money Mindset kanalı (ilk kanal)
-- ═══════════════════════════════════════════════════

INSERT INTO channels (
    id, name, description, niche, target_platform, status,
    content_language, weekly_video_target,
    background_color, primary_text_color, accent_color, secondary_accent_color,
    font_family, animation_style, watermark_text,
    ai_system_prompt, ai_script_template, content_tone,
    target_duration_seconds, target_scene_count
) VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Money Mindset Daily',
    'Personal finance tips, money rules, and wealth-building habits for YouTube Shorts',
    'MONEY_MINDSET',
    'YOUTUBE_SHORTS',
    'SETUP',
    'en',
    5,
    -- Görsel kimlik: koyu arka plan, altın vurgu
    '#0D0D0D',
    '#FFFFFF',
    '#FFD700',
    '#FF6B35',
    'Montserrat',
    'FADE_IN',
    '@MoneyMindsetDaily',
    -- AI Sistem Prompt
    'You are a content scriptwriter for a YouTube Shorts channel called "Money Mindset Daily".

Your job is to write short, punchy, high-retention scripts about personal finance, money management, and wealth-building habits.

RULES:
- Target audience: 18-45 year olds interested in building wealth
- Tone: Authoritative, direct, slightly provocative — like a rich mentor giving tough advice
- Every script MUST start with a powerful hook in the first 3 seconds
- Use simple, everyday language — no jargon
- Each script should have 6-8 text scenes (one key message per scene)
- End with a soft CTA (follow for more, or a thought-provoking question)
- NEVER give specific financial advice (no "buy this stock")
- NEVER mention specific amounts of money to invest
- Focus on mindset, habits, and principles
- Keep total reading time under 45 seconds',
    -- Script Template
    'Write a YouTube Shorts script about: {topic}

FORMAT (return EXACTLY this JSON structure):
{
  "title": "Video title for YouTube (max 60 chars, include emoji)",
  "hook": "First 3 seconds — provocative question or bold statement",
  "scenes": [
    {"text": "Scene 1 text (max 15 words)", "visualHint": "what image to show"},
    {"text": "Scene 2 text (max 15 words)", "visualHint": "what image to show"},
    ...
  ],
  "cta": "Closing call-to-action text",
  "hashtags": ["#money", "#finance", "#wealth"]
}

REQUIREMENTS:
- 6-8 scenes total
- Each scene text: max 15 words, punchy and readable
- Hook must create curiosity or FOMO
- Include at least one surprising fact or counterintuitive insight',
    'Authoritative, direct, slightly provocative',
    45,
    8
);
