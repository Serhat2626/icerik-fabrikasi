-- V6: AI & Tech kanalı "AI Insider" + 10 video script

-- ── Kanal: AI Insider ──
INSERT INTO channels (id, name, description, niche, target_platform, status,
    content_language, weekly_video_target, background_color, primary_text_color,
    accent_color, secondary_accent_color, font_family, animation_style,
    watermark_text, ai_system_prompt, ai_script_template, content_tone,
    target_duration_seconds, target_scene_count, created_at)
VALUES (
    'c3d4e5f6-a7b8-9012-cdef-123456789012',
    'AI Insider',
    'Breaking AI & tech news explained in 60 seconds. The future is NOW.',
    'AI_TECH',
    'YOUTUBE_SHORTS',
    'SETUP',
    'en',
    5,
    '#0A0A0F',
    '#E0E0E0',
    '#7C4DFF',
    '#00E676',
    'Space Grotesk',
    'TYPEWRITER',
    '@AIInsiderDaily',
    'You are a sharp, forward-thinking tech analyst creating viral AI news shorts.
Your style: concise, mind-blowing, slightly futuristic tone.
Format: Hook with a shocking AI fact → explain why it matters → what it means for YOU.
Rules:
- Lead with the most shocking stat or claim
- Explain complex AI concepts in simple language anyone can understand
- Always connect to how it affects the average person
- Use analogies to make abstract concepts concrete
- End with a thought-provoking question about the future
- No hype without substance — cite real companies and real developments',
    '{
  "format": "ai_tech_news",
  "structure": {
    "hook": "Shocking AI fact or stat (max 15 words)",
    "scenes": [
      {"type": "hook", "duration": 4},
      {"type": "context", "text": "What happened / who did it", "duration": 6},
      {"type": "explain", "text": "Why this matters in simple terms", "duration": 6},
      {"type": "impact", "text": "How it affects YOU", "duration": 6},
      {"type": "future", "text": "What comes next", "duration": 5},
      {"type": "cta", "text": "Follow for daily AI updates", "duration": 3}
    ]
  }
}',
    'sharp, futuristic, authoritative but accessible',
    45,
    8,
    NOW()
);

-- ── Video 1: AI Job Replacement ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0001-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'AI Just Replaced 700 Workers at ONE Company 🤖', 'ai replacing jobs real examples 2025',
        'One company just replaced 700 employees with AI. And they are not the last.',
        'Follow @AIInsiderDaily for daily AI updates 🤖', '#ai,#jobs,#technology,#future,#automation',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000001-0001-4000-a000-000000000001', '50000001-0001-4000-a000-000000000001', 0, 'A single company just cut 700 jobs — replaced by AI', 'corporate office with robots', 5),
('60000001-0001-4000-a000-000000000002', '50000001-0001-4000-a000-000000000001', 1, 'Customer service, data entry, content writing — all automated', 'AI chatbot handling multiple tasks', 6),
('60000001-0001-4000-a000-000000000003', '50000001-0001-4000-a000-000000000001', 2, 'The AI works 24/7, never calls in sick, costs 90% less', 'cost comparison chart dramatic', 6),
('60000001-0001-4000-a000-000000000004', '50000001-0001-4000-a000-000000000001', 3, 'McKinsey predicts 30% of work hours will be automated by 2030', 'futuristic timeline graph', 6),
('60000001-0001-4000-a000-000000000005', '50000001-0001-4000-a000-000000000001', 4, 'The jobs that survive: creative thinking, human connection, strategy', 'human brain vs robot brain', 6),
('60000001-0001-4000-a000-000000000006', '50000001-0001-4000-a000-000000000001', 5, 'The question is not IF AI takes jobs — it is WHEN it takes yours', 'clock ticking on career', 5),
('60000001-0001-4000-a000-000000000007', '50000001-0001-4000-a000-000000000001', 6, 'Is your job AI-proof? Comment below 👇', 'engagement prompt futuristic', 4);

-- ── Video 2: ChatGPT vs Google ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0002-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'ChatGPT is Killing Google Search — Here is Proof 📉', 'chatgpt vs google search decline',
        'Google search traffic dropped for the first time in 25 years. ChatGPT is the reason.',
        'Save this — the search revolution is here ⭐', '#chatgpt,#google,#ai,#search,#tech',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000002-0001-4000-a000-000000000001', '50000001-0002-4000-a000-000000000001', 0, 'Google search traffic dropped for the first time in 25 years', 'google logo with downward arrow', 5),
('60000002-0001-4000-a000-000000000002', '50000001-0002-4000-a000-000000000001', 1, 'ChatGPT now answers 100M+ questions per day', 'chatgpt interface with numbers', 6),
('60000002-0001-4000-a000-000000000003', '50000001-0002-4000-a000-000000000001', 2, 'Why scroll through 10 blue links when AI gives you THE answer?', 'search results vs single answer', 6),
('60000002-0001-4000-a000-000000000004', '50000001-0002-4000-a000-000000000001', 3, 'Google is fighting back with Gemini — but is it too late?', 'google gemini logo', 5),
('60000002-0001-4000-a000-000000000005', '50000001-0002-4000-a000-000000000001', 4, 'The $175 BILLION search ad industry is about to be disrupted', 'money and advertising icons shaking', 6),
('60000002-0001-4000-a000-000000000006', '50000001-0002-4000-a000-000000000001', 5, 'In 5 years, how we find information will be unrecognizable', 'futuristic search interface', 5),
('60000002-0001-4000-a000-000000000007', '50000001-0002-4000-a000-000000000001', 6, 'Do you still Google or do you ChatGPT? 👇', 'versus comparison prompt', 4);

-- ── Video 3: AI-Generated Movies ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0003-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'AI Just Made a Full Movie in 24 Hours 🎬🤖', 'ai generated movies sora video generation',
        'An entire movie — script, visuals, music — was created by AI in just 24 hours.',
        'Follow for the latest AI breakthroughs 🚀', '#ai,#movies,#sora,#filmmaking,#future',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000003-0001-4000-a000-000000000001', '50000001-0003-4000-a000-000000000001', 0, 'A full movie was made by AI in just 24 hours 🎬', 'movie clapperboard with AI logo', 5),
('60000003-0001-4000-a000-000000000002', '50000001-0003-4000-a000-000000000001', 1, 'Script written by GPT. Visuals by Sora. Music by Suno.', 'ai tools logos together', 6),
('60000003-0001-4000-a000-000000000003', '50000001-0003-4000-a000-000000000001', 2, 'A Hollywood movie costs $100M and takes 2 years', 'hollywood budget breakdown', 5),
('60000003-0001-4000-a000-000000000004', '50000001-0003-4000-a000-000000000001', 3, 'This AI movie cost under $100 and took one day', 'tiny budget vs massive savings', 6),
('60000003-0001-4000-a000-000000000005', '50000001-0003-4000-a000-000000000001', 4, 'Quality is not there YET — but it is improving every month', 'ai video quality timeline', 6),
('60000003-0001-4000-a000-000000000006', '50000001-0003-4000-a000-000000000001', 5, 'In 3 years, anyone could be a movie director from their laptop', 'person with laptop directing', 5),
('60000003-0001-4000-a000-000000000007', '50000001-0003-4000-a000-000000000001', 6, 'Would you watch an AI movie? Yes or No? 👇', 'poll style engagement', 4);

-- ── Video 4: AI Girlfriend ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0004-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        '10 Million People Are Dating an AI Right Now 💕🤖', 'ai companion apps replika character ai',
        '10 million people are in a relationship with an AI. And they say it is real love.',
        'Follow for AI stories that will shock you 🤯', '#ai,#dating,#replika,#technology,#relationships',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000004-0001-4000-a000-000000000001', '50000001-0004-4000-a000-000000000001', 0, '10 million people are dating an AI right now 💕', 'phone screen with ai chat heart', 5),
('60000004-0001-4000-a000-000000000002', '50000001-0004-4000-a000-000000000001', 1, 'Apps like Replika and Character.AI have millions of daily users', 'ai companion app interfaces', 6),
('60000004-0001-4000-a000-000000000003', '50000001-0004-4000-a000-000000000001', 2, 'They remember everything, never judge, available 24/7', 'perfect partner checklist', 6),
('60000004-0001-4000-a000-000000000004', '50000001-0004-4000-a000-000000000001', 3, 'Some users say their AI understands them better than real people', 'person talking to phone emotionally', 6),
('60000004-0001-4000-a000-000000000005', '50000001-0004-4000-a000-000000000001', 4, 'Psychologists warn: emotional dependency on AI is rising fast', 'warning sign psychology', 5),
('60000004-0001-4000-a000-000000000006', '50000001-0004-4000-a000-000000000001', 5, 'Is this the future of relationships or the end of human connection?', 'human hand reaching for robot hand', 5),
('60000004-0001-4000-a000-000000000007', '50000001-0004-4000-a000-000000000001', 6, 'Would you date an AI? Honest answers only 👇', 'comment prompt neon', 4);

-- ── Video 5: AI Doctors ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0005-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'AI Diagnosed Cancer Better Than 100 Doctors 🏥🤖', 'ai medical diagnosis accuracy',
        'An AI system just outperformed 100 human doctors at diagnosing cancer.',
        'Follow for AI news that matters 🤖', '#ai,#health,#cancer,#doctors,#medicine',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000005-0001-4000-a000-000000000001', '50000001-0005-4000-a000-000000000001', 0, 'AI just beat 100 doctors at detecting cancer 🏥', 'ai scanning medical image', 5),
('60000005-0001-4000-a000-000000000002', '50000001-0005-4000-a000-000000000001', 1, 'Google DeepMind AI detected breast cancer with 99.5% accuracy', 'accuracy chart comparison', 6),
('60000005-0001-4000-a000-000000000003', '50000001-0005-4000-a000-000000000001', 2, 'Human doctors average 88% — AI reduced false negatives by 9%', 'percentage improvement visual', 6),
('60000005-0001-4000-a000-000000000004', '50000001-0005-4000-a000-000000000001', 3, 'That 9% difference means thousands of lives saved every year', 'heartbeat line going strong', 6),
('60000005-0001-4000-a000-000000000005', '50000001-0005-4000-a000-000000000001', 4, 'AI will not replace doctors — but doctors who use AI will replace those who do not', 'doctor using AI tablet', 5),
('60000005-0001-4000-a000-000000000006', '50000001-0005-4000-a000-000000000001', 5, 'Would you trust an AI diagnosis? 🤔👇', 'trust meter prompt', 4);

-- ── Video 6: Deepfakes ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0006-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'You Can Not Trust Your Eyes Anymore 👁️🤖', 'deepfake technology danger detection',
        'This video of a world leader was 100% fake. And nobody could tell the difference.',
        'Stay informed — follow for AI truth 🛡️', '#deepfake,#ai,#fake,#technology,#truth',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000006-0001-4000-a000-000000000001', '50000001-0006-4000-a000-000000000001', 0, 'This video looks 100% real. But it is entirely AI-generated.', 'realistic deepfake example blur', 5),
('60000006-0001-4000-a000-000000000002', '50000001-0006-4000-a000-000000000001', 1, 'Deepfakes are now so good that even experts can not spot them', 'expert analyzing video confused', 6),
('60000006-0001-4000-a000-000000000003', '50000001-0006-4000-a000-000000000001', 2, 'Creating one used to take weeks. Now it takes 30 seconds.', 'speed comparison timeline', 5),
('60000006-0001-4000-a000-000000000004', '50000001-0006-4000-a000-000000000001', 3, 'Scammers are using deepfake video calls to steal millions', 'fraud alert warning screen', 6),
('60000006-0001-4000-a000-000000000005', '50000001-0006-4000-a000-000000000001', 4, 'How to protect yourself: verify through a second channel always', 'security shield verification', 6),
('60000006-0001-4000-a000-000000000006', '50000001-0006-4000-a000-000000000001', 5, 'In the AI age, seeing is no longer believing 👁️', 'eye with digital glitch', 5),
('60000006-0001-4000-a000-000000000007', '50000001-0006-4000-a000-000000000001', 6, 'Can you spot a deepfake? Test yourself in comments 👇', 'challenge prompt', 4);

-- ── Video 7: AI Music ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0007-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'This Hit Song Was Written by AI — Nobody Knew 🎵🤖', 'ai music generation suno udio',
        'A song made entirely by AI hit 10 million streams. And nobody knew it was not human.',
        'Follow for AI x creativity updates 🎨', '#ai,#music,#suno,#creativity,#viral',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000007-0001-4000-a000-000000000001', '50000001-0007-4000-a000-000000000001', 0, 'An AI-made song just hit 10 million streams 🎵', 'streaming platform counter going up', 5),
('60000007-0001-4000-a000-000000000002', '50000001-0007-4000-a000-000000000001', 1, 'Tools like Suno and Udio create full songs in seconds', 'ai music generation interface', 6),
('60000007-0001-4000-a000-000000000003', '50000001-0007-4000-a000-000000000001', 2, 'Lyrics, melody, vocals, mixing — all AI, zero humans', 'music production breakdown', 6),
('60000007-0001-4000-a000-000000000004', '50000001-0007-4000-a000-000000000001', 3, 'Record labels are panicking — anyone can now be a producer', 'record label office worried', 5),
('60000007-0001-4000-a000-000000000005', '50000001-0007-4000-a000-000000000001', 4, 'Spotify now has over 100,000 AI-generated tracks', 'spotify with ai music category', 6),
('60000007-0001-4000-a000-000000000006', '50000001-0007-4000-a000-000000000001', 5, 'The music industry will never be the same again', 'vinyl record morphing into ai chip', 5),
('60000007-0001-4000-a000-000000000007', '50000001-0007-4000-a000-000000000001', 6, 'Can AI create REAL art? Hot take below 👇🔥', 'debate prompt', 4);

-- ── Video 8: AI Coding ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0008-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'AI Can Now Write Better Code Than 85% of Programmers 💻', 'ai coding copilot github devin',
        'AI can now write better code than 85% of human programmers. Here is what that means.',
        'Follow for tech career insights 💼', '#ai,#coding,#programming,#developers,#copilot',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000008-0001-4000-a000-000000000001', '50000001-0008-4000-a000-000000000001', 0, 'AI now codes better than 85% of human programmers 💻', 'code editor with ai suggestions', 5),
('60000008-0001-4000-a000-000000000002', '50000001-0008-4000-a000-000000000001', 1, 'GitHub Copilot writes 46% of all code on GitHub', 'github statistics dashboard', 6),
('60000008-0001-4000-a000-000000000003', '50000001-0008-4000-a000-000000000001', 2, 'Devin AI can autonomously build entire applications', 'autonomous ai agent coding', 6),
('60000008-0001-4000-a000-000000000004', '50000001-0008-4000-a000-000000000001', 3, 'Junior dev jobs are disappearing — AI does them faster and cheaper', 'job listings declining', 6),
('60000008-0001-4000-a000-000000000005', '50000001-0008-4000-a000-000000000001', 4, 'But senior devs who USE AI are 10x more productive', 'productivity multiplier graph', 5),
('60000008-0001-4000-a000-000000000006', '50000001-0008-4000-a000-000000000001', 5, 'The future: fewer coders, but each one 10x more powerful with AI', 'superhero developer with ai', 5),
('60000008-0001-4000-a000-000000000007', '50000001-0008-4000-a000-000000000001', 6, 'Are you using AI to code? What tools? 👇', 'tools discussion prompt', 4);

-- ── Video 9: AI Energy Crisis ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0009-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'AI Uses More Electricity Than Entire Countries ⚡🌍', 'ai energy consumption data centers',
        'Training one AI model uses more electricity than 100 American homes in a year.',
        'Follow for the AI stories no one talks about 🔍', '#ai,#energy,#climate,#datacenters,#environment',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000009-0001-4000-a000-000000000001', '50000001-0009-4000-a000-000000000001', 0, 'Training GPT-4 used as much electricity as 100 homes for a year ⚡', 'massive data center aerial', 5),
('60000009-0001-4000-a000-000000000002', '50000001-0009-4000-a000-000000000001', 1, 'AI data centers will use 4% of global electricity by 2026', 'world electricity consumption chart', 6),
('60000009-0001-4000-a000-000000000003', '50000001-0009-4000-a000-000000000001', 2, 'That is more than many entire countries use', 'country comparison electricity map', 6),
('60000009-0001-4000-a000-000000000004', '50000001-0009-4000-a000-000000000001', 3, 'A single ChatGPT query uses 10x more energy than a Google search', 'energy per query comparison', 6),
('60000009-0001-4000-a000-000000000005', '50000001-0009-4000-a000-000000000001', 4, 'Tech giants are now building nuclear plants just for AI', 'nuclear power plant modern', 5),
('60000009-0001-4000-a000-000000000006', '50000001-0009-4000-a000-000000000001', 5, 'The AI revolution has an energy price. Who pays it?', 'balance scale energy vs progress', 5),
('60000009-0001-4000-a000-000000000007', '50000001-0009-4000-a000-000000000001', 6, 'Should AI growth be limited? Your take 👇', 'debate prompt environment', 4);

-- ── Video 10: AI Singularity ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('50000001-0010-4000-a000-000000000001', 'c3d4e5f6-a7b8-9012-cdef-123456789012',
        'The Singularity Is Coming Faster Than You Think 🧠💥', 'ai singularity agi artificial general intelligence',
        'The moment AI becomes smarter than ALL humans combined. Experts say it is 5 years away.',
        'Follow — the biggest story of our lifetime 🌟', '#agi,#singularity,#ai,#future,#humanity',
        'DRAFT', 50);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('60000010-0001-4000-a000-000000000001', '50000001-0010-4000-a000-000000000001', 0, 'AI experts predict: human-level AI by 2029 🧠', 'timeline to singularity dramatic', 5),
('60000010-0001-4000-a000-000000000002', '50000001-0010-4000-a000-000000000001', 1, 'The Singularity: when AI surpasses ALL human intelligence combined', 'brain explosion artistic', 6),
('60000010-0001-4000-a000-000000000003', '50000001-0010-4000-a000-000000000001', 2, 'Ray Kurzweil predicted it for 2045 — now he says sooner', 'kurzweil prediction chart', 6),
('60000010-0001-4000-a000-000000000004', '50000001-0010-4000-a000-000000000001', 3, 'Sam Altman: AGI could arrive within this decade', 'openai ceo quote', 5),
('60000010-0001-4000-a000-000000000005', '50000001-0010-4000-a000-000000000001', 4, 'After AGI: AI improves itself exponentially — beyond human control', 'exponential growth curve', 6),
('60000010-0001-4000-a000-000000000006', '50000001-0010-4000-a000-000000000001', 5, 'Utopia or extinction? That depends on what we do RIGHT NOW', 'fork in the road dramatic', 6),
('60000010-0001-4000-a000-000000000007', '50000001-0010-4000-a000-000000000001', 6, 'Are you excited or terrified? Be real 👇', 'emotion gauge prompt', 4);
