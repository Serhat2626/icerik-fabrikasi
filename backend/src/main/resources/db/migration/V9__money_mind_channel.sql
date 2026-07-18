-- =====================================================================
-- V9: Money Mind Kanal Konfigürasyonu
-- =====================================================================
-- İlk Shorts kanalı: 💰 Money Mind — Kişisel Finans
-- Hedef: ABD/İngiltere, İngilizce, yüksek CPM finans nişi
-- Affiliate odaklı: Wise, Revolut, Trading 212, Acorns

INSERT INTO channels (
    id, created_at, updated_at,
    name, description, niche, target_platform, status,
    content_language, weekly_video_target,
    -- Görsel kimlik
    background_color, primary_text_color, accent_color, secondary_accent_color,
    font_family, animation_style, watermark_text,
    -- Ses konfigürasyonu
    voice_id, voice_model, voice_stability, voice_similarity_boost, voice_style,
    -- Görsel üretim
    image_style_preset, hook_font_size, body_font_size, transition_type,
    -- AI prompt
    ai_system_prompt, ai_script_template, content_tone,
    target_duration_seconds, target_scene_count,
    -- Metrikler
    total_videos_produced, total_videos_published
) VALUES (
    gen_random_uuid(), NOW(), NOW(),
    'Money Mind',
    'Personal finance tips, money rules, and wealth-building strategies. Targeting US/UK audience with high-CPM finance content.',
    'MONEY_MINDSET',
    'YOUTUBE_SHORTS',
    'ACTIVE',
    'en',
    7,
    -- Görsel kimlik: Koyu lacivert + altın + yeşil
    '#0A0E27',    -- backgroundColor: derin lacivert
    '#FFFFFF',    -- primaryTextColor: beyaz
    '#FFD700',    -- accentColor: altın
    '#00D26A',    -- secondaryAccentColor: yeşil (para rengi)
    'Montserrat', -- fontFamily: modern, bold
    'WORD_BY_WORD',
    '@MoneyMindDaily',
    -- Ses: Josh — otoriter, güven veren erkek ses
    'pNInz6obpgDQGcFmaJgB',  -- Josh voice ID
    'eleven_turbo_v2_5',
    0.45,   -- stability: biraz düşük = daha doğal
    0.78,   -- similarity: yüksek = sese sadık
    0.35,   -- style: hafif dramatik
    -- Görsel üretim stili
    'Dark luxury background with deep navy blue tones, golden warm accents, cinematic financial concept art. Professional, premium, high-contrast.',
    96, 72,
    'DISSOLVE',
    -- AI sistem prompt'u
    'You are a professional personal finance content writer creating YouTube Shorts scripts.
Your target audience is 18-35 year olds in the US and UK who want to build wealth.
Write in a confident, authoritative but approachable tone.
Every script must have a shocking or counterintuitive hook in the first sentence.
Use concrete numbers and data points.
Avoid financial advice disclaimers in the script (they go in the description).
Format: Hook → 3-5 actionable points → Strong closer.
Language: English only.
Never use emojis in the script text.',

    'Topic: {topic}

Write a YouTube Shorts script (30-45 seconds) about this financial topic.

Structure:
1. HOOK (1 sentence, shocking/counterintuitive, must stop the scroll)
2. SCENES (3-5 scenes, each 1-2 sentences with a visual_hint)
3. CTA (1 sentence, engagement-focused)

Example hooks:
- "You are losing $1,000 every year by doing THIS"
- "5 money rules the rich never break"
- "Stop saving money. Do this instead."

Return JSON:
{
  "title": "YouTube title (max 60 chars, curiosity-gap)",
  "hook": "Provocative opening line",
  "scenes": [
    {"text": "Scene text", "visual_hint": "Visual description", "duration_seconds": 5}
  ],
  "cta": "Follow for more money tips",
  "hashtags": "#money,#finance,#personalfinance,#investing,#wealthtips"
}',

    'authoritative, confident, direct, data-driven',
    35,    -- targetDurationSeconds
    5,     -- targetSceneCount
    0, 0
) ON CONFLICT DO NOTHING;
