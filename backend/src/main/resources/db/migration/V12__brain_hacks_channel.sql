-- =====================================================================
-- V12: Brain Hacks Kanal Konfigürasyonu + Affiliate Linkler
-- =====================================================================
-- 🧠 Brain Hacks — Psikoloji & İnsan Davranışı
-- Merak döngüsü formatı — çok yüksek engagement
-- Affiliate: BetterHelp ($100+), Headspace, Audible, Skillshare

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
    'Brain Hacks',
    'Why does your brain do THAT? Psychology facts, cognitive biases, and mind-blowing human behavior explained.',
    'PSYCHOLOGY',
    'YOUTUBE_SHORTS',
    'ACTIVE',
    'en',
    5,
    '#0D1B2A',
    '#E0E0E0',
    '#FF8C42',
    '#48BFE3',
    'Nunito',
    'FADE_IN',
    '@BrainHacksDaily',
    '21m00Tcm4TlvDq8ikWAM',
    'eleven_turbo_v2_5',
    0.55,
    0.70,
    0.25,
    'Surreal dreamlike psychology art with teal and warm orange tones. Abstract brain visualizations, cognitive patterns, optical illusions. Ethereal, mysterious atmosphere with deep shadows.',
    96, 72,
    'FADE',
    'You are a fascinating psychology content writer creating YouTube Shorts scripts.
Your target audience is curious minds aged 16-35 who love learning about human behavior.
Write in an intriguing, slightly mysterious tone — like revealing a secret about the mind.
Every script must create a "curiosity loop" — make viewers NEED to know the answer.
Use relatable everyday examples that make viewers say "OMG that is so true!"
Reference real psychological studies or phenomena by name when possible.
Format: Curiosity hook → Explanation with examples → Mind-blowing conclusion
Language: English only.
Never use emojis in the script text.',
    'Topic: {topic}

Write a YouTube Shorts script (30-45 seconds) about this psychology topic.

Structure:
1. HOOK (1 sentence, must trigger curiosity — "why" or "did you know" format)
2. SCENES (3-5 scenes explaining the psychology)
3. CTA (1 sentence, question format to drive comments)

Example hooks:
- "Your brain is lying to you right now and you do not even know it"
- "There is a reason you check your phone 150 times a day"
- "95 percent of your decisions are made by your unconscious mind"

Return JSON:
{
  "title": "YouTube title (max 60 chars, curiosity-gap)",
  "hook": "Intriguing opening question or statement",
  "scenes": [
    {"text": "Scene text", "visual_hint": "Visual description", "duration_seconds": 6}
  ],
  "cta": "Which brain hack surprised you most? Comment below!",
  "hashtags": "#psychology,#brainhacks,#mindblown,#humanmind,#facts"
}',
    'intriguing, mysterious, mind-blowing, conversational',
    40,
    4,
    '⚠️ This content is for educational purposes only. It is not a substitute for professional medical or psychological advice. If you are struggling, please reach out to a licensed professional.',
    0, 0
) ON CONFLICT DO NOTHING;

-- Brain Hacks affiliate linkleri
WITH bh_channel AS (
    SELECT id FROM channels WHERE name = 'Brain Hacks' LIMIT 1
)
INSERT INTO affiliate_links (id, created_at, updated_at, channel_id, product_name, affiliate_url, commission_type, commission_value, display_text, keywords, priority, active, emoji)
SELECT
    gen_random_uuid(), NOW(), NOW(), bc.id,
    v.product_name, v.affiliate_url, v.commission_type, v.commission_value,
    v.display_text, v.keywords, v.priority, TRUE, v.emoji
FROM bh_channel bc,
(VALUES
    ('BetterHelp', 'https://betterhelp.com/brainhacks', 'CPA', '$100-200', 'Talk to a therapist today — BetterHelp', 'therapy,mental,health,anxiety,depression,stress,help', 1, '💚'),
    ('Headspace', 'https://headspace.com/brainhacks', 'ONE_TIME', '$10-15', 'Start meditating with Headspace', 'meditation,mindfulness,calm,sleep,stress,relax', 2, '🧘'),
    ('Audible', 'https://audible.com/brainhacks', 'CPA', '$5-15 per trial', 'Listen FREE on Audible — psychology audiobooks', 'book,read,listen,audiobook,psychology,learn', 3, '🎧'),
    ('Skillshare', 'https://skillshare.com/brainhacks', 'ONE_TIME', '$7-10', 'Master your mind on Skillshare', 'learn,course,skill,creative,class,education', 4, '🎓'),
    ('Brilliant.org', 'https://brilliant.org/brainhacks', 'ONE_TIME', '$20', 'Train your brain with Brilliant', 'science,math,logic,puzzle,brain,think,learn', 5, '🧠')
) AS v(product_name, affiliate_url, commission_type, commission_value, display_text, keywords, priority, emoji);
