-- =====================================================================
-- V11: AI in 60 Seconds Kanal Konfigürasyonu + Affiliate Linkler
-- =====================================================================
-- 🤖 AI in 60 Seconds — AI Araç Demoları
-- Hedef: Global, İngilizce, +340% büyüyen AI nişi
-- EN HIZLI PARA: Hostinger %60 affiliate + SaaS recurring

INSERT INTO channels (
    id, created_at, updated_at,
    name, description, niche, target_platform, status,
    content_language, weekly_video_target,
    background_color, primary_text_color, accent_color, secondary_accent_color,
    font_family, animation_style, watermark_text,
    voice_id, voice_model, voice_stability, voice_similarity_boost, voice_style,
    image_style_preset, hook_font_size, body_font_size, transition_type,
    ai_system_prompt, ai_script_template, content_tone,
    target_duration_seconds, target_scene_count,
    description_template,
    total_videos_produced, total_videos_published
) VALUES (
    gen_random_uuid(), NOW(), NOW(),
    'AI in 60 Seconds',
    'Mind-blowing AI tools demonstrated in 60 seconds or less. New tools every day.',
    'AI_TECH',
    'YOUTUBE_SHORTS',
    'ACTIVE',
    'en',
    7,
    '#0D0D1A',
    '#FFFFFF',
    '#00D4FF',
    '#7B2FFF',
    'Space Grotesk',
    'SLIDE_UP',
    '@AIin60Seconds',
    'pNInz6obpgDQGcFmaJgB',
    'eleven_turbo_v2_5',
    0.50,
    0.80,
    0.40,
    'Futuristic dark technology background with neon blue and purple glowing elements. Clean digital interface aesthetic, holographic effects, sci-fi atmosphere. Ultra modern, sleek.',
    96, 72,
    'SLIDE_LEFT',
    'You are an energetic AI tools content writer creating YouTube Shorts scripts.
Your target audience is tech-curious professionals and creators aged 18-40.
Write with excitement and wonder — every AI tool should feel like magic.
Always lead with the WOW factor — what this tool can do that seems impossible.
Include specific use cases and results.
Keep it fast-paced: short punchy sentences.
Format: Mind-blowing hook → Tool demo description → Result → CTA
Language: English only.
Never use emojis in the script text.',
    'Topic: {topic}

Write a YouTube Shorts script (30-45 seconds) about this AI tool or concept.

Structure:
1. HOOK (1 sentence, must create "wait... WHAT?" reaction)
2. SCENES (3-5 scenes showing what the tool does)
3. CTA (1 sentence, follow-focused)

Example hooks:
- "This AI just replaced a $5000/month employee"
- "I gave this AI my face and it did SOMETHING INSANE"
- "This free AI tool does what Photoshop can not"

Return JSON:
{
  "title": "YouTube title (max 60 chars, WOW factor)",
  "hook": "Mind-blowing opening line",
  "scenes": [
    {"text": "Scene text", "visual_hint": "Visual description", "duration_seconds": 5}
  ],
  "cta": "Follow for daily AI tools",
  "hashtags": "#ai,#aitools,#technology,#tech,#artificialintelligence"
}',
    'excited, energetic, mind-blown, fast-paced',
    35,
    5,
    '⚠️ Tool features and pricing may change. Always verify on the official website.',
    0, 0
) ON CONFLICT DO NOTHING;

-- AI in 60 Seconds affiliate linkleri
WITH ai_channel AS (
    SELECT id FROM channels WHERE name = 'AI in 60 Seconds' LIMIT 1
)
INSERT INTO affiliate_links (id, created_at, updated_at, channel_id, product_name, affiliate_url, commission_type, commission_value, display_text, keywords, priority, active, emoji)
SELECT
    gen_random_uuid(), NOW(), NOW(), ac.id,
    v.product_name, v.affiliate_url, v.commission_type, v.commission_value,
    v.display_text, v.keywords, v.priority, TRUE, v.emoji
FROM ai_channel ac,
(VALUES
    ('Hostinger', 'https://hostinger.com/ai60s', 'REVENUE_SHARE', '60%', 'Build your AI website with Hostinger', 'hosting,website,build,deploy,domain,server', 1, '🌐'),
    ('Canva Pro', 'https://canva.com/ai60s', 'ONE_TIME', '$18-36', 'Design anything with Canva Pro AI', 'design,canva,graphic,poster,thumbnail,logo', 2, '🎨'),
    ('NordVPN', 'https://nordvpn.com/ai60s', 'REVENUE_SHARE', '40-100% + 30% renewal', 'Protect your AI data with NordVPN', 'privacy,security,vpn,protect,data,hack', 3, '🔒'),
    ('Writesonic', 'https://writesonic.com/ai60s', 'RECURRING', '30% recurring', 'Write 10x faster with Writesonic AI', 'writing,content,blog,copy,text,article', 4, '✍️'),
    ('Notion', 'https://notion.so/ai60s', 'RECURRING', '50% first year', 'Organize everything with Notion AI', 'notes,organize,productivity,workspace,notion', 5, '📝')
) AS v(product_name, affiliate_url, commission_type, commission_value, display_text, keywords, priority, emoji);
