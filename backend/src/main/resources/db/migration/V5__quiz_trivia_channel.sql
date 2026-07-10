-- V5: Quiz & Trivia kanalı "Brain Buzz" + 10 video script

-- ── Kanal: Brain Buzz ──
INSERT INTO channels (id, name, description, niche, target_platform, status,
    content_language, weekly_video_target, background_color, primary_text_color,
    accent_color, secondary_accent_color, font_family, animation_style,
    watermark_text, ai_system_prompt, ai_script_template, content_tone,
    target_duration_seconds, target_scene_count, created_at)
VALUES (
    'b2c3d4e5-f6a7-8901-bcde-f12345678901',
    'Brain Buzz',
    'Fun quiz and trivia shorts that test your knowledge. Can you beat the clock?',
    'QUIZ_TRIVIA',
    'YOUTUBE_SHORTS',
    'SETUP',
    'en',
    5,
    '#1A0A2E',
    '#FFFFFF',
    '#00E5FF',
    '#FF4081',
    'Poppins',
    'BOUNCE',
    '@BrainBuzzQuiz',
    'You are a fun, energetic quiz host creating engaging trivia content for YouTube Shorts.
Your style: game-show energy, quick-fire questions, surprising answers.
Format: Ask a question → give thinking time → reveal the answer with a fun fact.
Rules:
- Questions should be surprising and make people say "I did not know that!"
- Use emojis for visual engagement
- Keep answers short but memorable
- Always include a "Did you know?" fact after the answer
- End with a teaser for the next question',
    '{
  "format": "quiz_trivia",
  "structure": {
    "question": "The main trivia question (max 15 words)",
    "options": ["A) ...", "B) ...", "C) ...", "D) ..."],
    "correct_answer": "The correct option letter and text",
    "fun_fact": "A surprising fact related to the answer",
    "difficulty": "easy|medium|hard"
  },
  "scenes": [
    {"type": "question", "text": "The question text", "duration": 5},
    {"type": "options", "text": "Show all 4 options", "duration": 6},
    {"type": "thinking", "text": "Think fast! ⏰", "duration": 3},
    {"type": "reveal", "text": "Answer + explanation", "duration": 6},
    {"type": "fun_fact", "text": "Did you know? ...", "duration": 5},
    {"type": "cta", "text": "Follow for more!", "duration": 3}
  ]
}',
    'fun, energetic, game-show host',
    40,
    7,
    NOW()
);

-- ── Video 1: World Geography ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0001-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'Only 5% Get This Geography Question Right 🌍', 'world geography quiz capital cities',
        'Can you name the only country in the world that spans 4 hemispheres?',
        'Follow @BrainBuzzQuiz for daily brain teasers! 🧠', '#quiz,#trivia,#geography,#brainteaser,#didyouknow',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000001-0001-4000-a000-000000000001', '30000001-0001-4000-a000-000000000001', 0, 'Which country spans ALL 4 hemispheres? 🌍', 'spinning globe animation', 5),
('40000001-0001-4000-a000-000000000002', '30000001-0001-4000-a000-000000000001', 1, 'A) Brazil  B) Indonesia  C) Kiribati  D) Russia', 'four option cards colorful', 6),
('40000001-0001-4000-a000-000000000003', '30000001-0001-4000-a000-000000000001', 2, 'Think fast! ⏰ 3... 2... 1...', 'countdown timer ticking', 4),
('40000001-0001-4000-a000-000000000004', '30000001-0001-4000-a000-000000000001', 3, '✅ C) KIRIBATI! A tiny island nation in the Pacific', 'kiribati islands aerial view', 6),
('40000001-0001-4000-a000-000000000005', '30000001-0001-4000-a000-000000000001', 4, 'Fun fact: Kiribati is so spread out it has 3 different time zones! ⏰', 'world map with time zones', 5),
('40000001-0001-4000-a000-000000000006', '30000001-0001-4000-a000-000000000001', 5, 'Did you get it right? Comment your answer! 👇', 'engagement prompt neon', 4);

-- ── Video 2: Human Body ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0002-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'Your Body Does THIS 23,000 Times a Day 😱', 'human body amazing facts quiz',
        'Your body does something 23,000 times every day without you even thinking about it.',
        'Save this and test your friends! ⭐', '#body,#science,#quiz,#facts,#amazing',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000002-0001-4000-a000-000000000001', '30000001-0002-4000-a000-000000000001', 0, 'What does your body do 23,000 times EVERY day? 🤔', 'human body silhouette glowing', 5),
('40000002-0001-4000-a000-000000000002', '30000001-0002-4000-a000-000000000001', 1, 'A) Blink  B) Breathe  C) Swallow  D) Yawn', 'four option cards medical theme', 6),
('40000002-0001-4000-a000-000000000003', '30000001-0002-4000-a000-000000000001', 2, 'Lock in your answer! ⏰', 'thinking brain animation', 3),
('40000002-0001-4000-a000-000000000004', '30000001-0002-4000-a000-000000000001', 3, '✅ B) BREATHE! You take about 23,040 breaths daily', 'lungs breathing animation', 6),
('40000002-0001-4000-a000-000000000005', '30000001-0002-4000-a000-000000000001', 4, 'Fun fact: In a lifetime, you breathe about 600 million times! 💨', 'massive number counter', 5),
('40000002-0001-4000-a000-000000000006', '30000001-0002-4000-a000-000000000001', 5, 'Drop a 🫁 if you got it right!', 'engagement prompt colorful', 4);

-- ── Video 3: Space ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0003-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'This Planet Rains DIAMONDS 💎🪐', 'space facts quiz planet trivia',
        'There is a planet where it literally rains diamonds. Can you guess which one?',
        'Follow for mind-blowing facts! 🚀', '#space,#planets,#diamonds,#quiz,#science',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000003-0001-4000-a000-000000000001', '30000001-0003-4000-a000-000000000001', 0, 'On which planet does it RAIN DIAMONDS? 💎', 'diamond rain artistic render', 5),
('40000003-0001-4000-a000-000000000002', '30000001-0003-4000-a000-000000000001', 1, 'A) Jupiter  B) Neptune  C) Venus  D) Mars', 'solar system with highlighted planets', 6),
('40000003-0001-4000-a000-000000000003', '30000001-0003-4000-a000-000000000001', 2, 'Choose wisely! ⏰ 3... 2... 1...', 'sparkle countdown timer', 4),
('40000003-0001-4000-a000-000000000004', '30000001-0003-4000-a000-000000000001', 3, '✅ B) NEPTUNE! Carbon atoms compress into diamonds', 'neptune planet closeup', 6),
('40000003-0001-4000-a000-000000000005', '30000001-0003-4000-a000-000000000001', 4, 'Fun fact: Jupiter also has diamond rain — up to 10 million tons per year! 🤯', 'jupiter storm closeup', 5),
('40000003-0001-4000-a000-000000000006', '30000001-0003-4000-a000-000000000001', 5, 'Which planet surprised you most? Comment! 🪐', 'comment prompt space theme', 4);

-- ── Video 4: Food Facts ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0004-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'This Food Never Expires — EVER 🍯', 'food that never expires honey facts',
        'One food has been found in Egyptian tombs and was STILL edible after 3000 years.',
        'Share this with a foodie! 🍽️', '#food,#facts,#honey,#quiz,#history',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000004-0001-4000-a000-000000000001', '30000001-0004-4000-a000-000000000001', 0, 'Which food NEVER expires — not even after 3000 years? 🤔', 'ancient egyptian tomb interior', 5),
('40000004-0001-4000-a000-000000000002', '30000001-0004-4000-a000-000000000001', 1, 'A) Rice  B) Honey  C) Salt  D) Sugar', 'four food items on display', 6),
('40000004-0001-4000-a000-000000000003', '30000001-0004-4000-a000-000000000001', 2, 'Time is up! ⏰', 'buzzer going off', 3),
('40000004-0001-4000-a000-000000000004', '30000001-0004-4000-a000-000000000001', 3, '✅ B) HONEY! Found edible in 3000-year-old Egyptian tombs', 'golden honey pouring closeup', 6),
('40000004-0001-4000-a000-000000000005', '30000001-0004-4000-a000-000000000001', 4, 'Fun fact: Honey has natural antibacterial properties that prevent spoilage! 🐝', 'bees making honey in hive', 5),
('40000004-0001-4000-a000-000000000006', '30000001-0004-4000-a000-000000000001', 5, 'Did you know this? 🍯 or 🤯 in comments!', 'engagement prompt', 4);

-- ── Video 5: Animal Kingdom ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0005-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'This Animal Can Hold Its Breath for 2 HOURS 🐢', 'animal world records quiz',
        'One animal can hold its breath for over 2 hours underwater. Can you guess?',
        'Follow for daily quizzes! 🧠', '#animals,#nature,#quiz,#wildlife,#amazing',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000005-0001-4000-a000-000000000001', '30000001-0005-4000-a000-000000000001', 0, 'Which animal holds its breath for over 2 HOURS? 🫧', 'underwater scene mysterious', 5),
('40000005-0001-4000-a000-000000000002', '30000001-0005-4000-a000-000000000001', 1, 'A) Dolphin  B) Sea Turtle  C) Whale  D) Crocodile', 'four animal silhouettes', 6),
('40000005-0001-4000-a000-000000000003', '30000001-0005-4000-a000-000000000001', 2, 'Final answer? ⏰', 'dramatic music buildup', 3),
('40000005-0001-4000-a000-000000000004', '30000001-0005-4000-a000-000000000001', 3, '✅ B) SEA TURTLE! Some species hold breath for up to 7 hours!', 'sea turtle swimming gracefully', 6),
('40000005-0001-4000-a000-000000000005', '30000001-0005-4000-a000-000000000001', 4, 'Fun fact: Sea turtles slow their heart rate to 1 beat per 9 minutes! 💓', 'turtle heartbeat animation', 5),
('40000005-0001-4000-a000-000000000006', '30000001-0005-4000-a000-000000000001', 5, 'Tag someone who would fail this quiz 😂👇', 'fun engagement prompt', 4);

-- ── Video 6: History ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0006-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'Cleopatra Was Closer to the iPhone Than the Pyramids 📱🏛️', 'mind-blowing history timeline facts',
        'Cleopatra lived closer in time to the iPhone than to the building of the Great Pyramids.',
        'Save this to share later! ⭐', '#history,#cleopatra,#pyramids,#mindblown,#facts',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000006-0001-4000-a000-000000000001', '30000001-0006-4000-a000-000000000001', 0, 'TRUE or FALSE: Cleopatra lived closer to the iPhone than the Pyramids 🤔', 'cleopatra with iphone funny art', 5),
('40000006-0001-4000-a000-000000000002', '30000001-0006-4000-a000-000000000001', 1, 'Most people get this WRONG', 'thinking face emoji large', 4),
('40000006-0001-4000-a000-000000000003', '30000001-0006-4000-a000-000000000001', 2, '✅ TRUE! The Great Pyramids: 2560 BC', 'pyramids of giza', 5),
('40000006-0001-4000-a000-000000000004', '30000001-0006-4000-a000-000000000001', 3, 'Cleopatra: 69-30 BC — that is a 2500 year gap!', 'cleopatra portrait ancient', 5),
('40000006-0001-4000-a000-000000000005', '30000001-0006-4000-a000-000000000001', 4, 'iPhone released: 2007 AD — only 2037 years after Cleopatra! 📱', 'iphone original launch', 5),
('40000006-0001-4000-a000-000000000006', '30000001-0006-4000-a000-000000000001', 5, 'History is wilder than you think 🤯 Follow for more!', 'mind blown animation', 4);

-- ── Video 7: Language ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0007-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'The Most Spoken Language is NOT English 🗣️', 'most spoken languages world quiz',
        'What is the most spoken language in the world? It is NOT English.',
        'Follow for daily brain teasers! 🧠', '#languages,#quiz,#world,#culture,#facts',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000007-0001-4000-a000-000000000001', '30000001-0007-4000-a000-000000000001', 0, 'What is the MOST spoken language in the world? 🗣️', 'world map with speech bubbles', 5),
('40000007-0001-4000-a000-000000000002', '30000001-0007-4000-a000-000000000001', 1, 'A) English  B) Spanish  C) Mandarin  D) Hindi', 'four flags representing languages', 6),
('40000007-0001-4000-a000-000000000003', '30000001-0007-4000-a000-000000000001', 2, 'Answer now! ⏰', 'clock ticking dramatic', 3),
('40000007-0001-4000-a000-000000000004', '30000001-0007-4000-a000-000000000001', 3, '✅ C) MANDARIN CHINESE! 1.1 billion native speakers', 'chinese characters beautiful calligraphy', 6),
('40000007-0001-4000-a000-000000000005', '30000001-0007-4000-a000-000000000001', 4, 'Fun fact: English is 3rd by native speakers but 1st when including second-language speakers!', 'global language statistics chart', 5),
('40000007-0001-4000-a000-000000000006', '30000001-0007-4000-a000-000000000001', 5, 'How many languages do YOU speak? Comment! 🌍', 'multilingual word cloud', 4);

-- ── Video 8: Math Puzzle ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0008-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        '95% of Adults Get This Math Wrong ➗🤯', 'viral math puzzle order of operations',
        '95% of adults get this simple math problem wrong. Let us see if you can solve it.',
        'Share and see who gets it right! 📩', '#math,#puzzle,#quiz,#brainteaser,#viral',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000008-0001-4000-a000-000000000001', '30000001-0008-4000-a000-000000000001', 0, 'Solve this: 8 ÷ 2(2+2) = ? 🤔', 'chalkboard with equation', 5),
('40000008-0001-4000-a000-000000000002', '30000001-0008-4000-a000-000000000001', 1, 'Most people say 1... but is that right?', 'person thinking hard', 5),
('40000008-0001-4000-a000-000000000003', '30000001-0008-4000-a000-000000000001', 2, 'Step 1: Parentheses first → 2+2 = 4', 'equation step by step', 5),
('40000008-0001-4000-a000-000000000004', '30000001-0008-4000-a000-000000000001', 3, 'Step 2: Left to right → 8÷2 = 4, then 4×4 = 16', 'final answer reveal 16', 5),
('40000008-0001-4000-a000-000000000005', '30000001-0008-4000-a000-000000000001', 4, '✅ The answer is 16! PEMDAS left-to-right rule', 'big number 16 celebration', 5),
('40000008-0001-4000-a000-000000000006', '30000001-0008-4000-a000-000000000001', 5, 'Did you get 1 or 16? Be honest! 👇', 'poll style engagement', 4);

-- ── Video 9: Technology ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0009-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'Your Phone is 100,000x Stronger Than Apollo 11 🚀📱', 'technology comparison facts quiz',
        'The phone in your pocket is 100,000 times more powerful than the computer that landed on the Moon.',
        'Follow for more tech facts! 🤖', '#technology,#space,#phone,#facts,#apollo',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000009-0001-4000-a000-000000000001', '30000001-0009-4000-a000-000000000001', 0, 'How much more powerful is your phone vs Apollo 11 computer? 🚀', 'apollo 11 mission control', 5),
('40000009-0001-4000-a000-000000000002', '30000001-0009-4000-a000-000000000001', 1, 'A) 10x  B) 1,000x  C) 100,000x  D) 1 million x', 'comparison scale graphic', 6),
('40000009-0001-4000-a000-000000000003', '30000001-0009-4000-a000-000000000001', 2, 'Think about it... ⏰', 'thinking pose astronaut', 3),
('40000009-0001-4000-a000-000000000004', '30000001-0009-4000-a000-000000000001', 3, '✅ C) 100,000 times! Apollo had 74KB of memory', 'apollo computer vs modern phone', 6),
('40000009-0001-4000-a000-000000000005', '30000001-0009-4000-a000-000000000001', 4, 'Fun fact: A musical greeting card has more computing power than Apollo 11! 🎵', 'greeting card vs spacecraft', 5),
('40000009-0001-4000-a000-000000000006', '30000001-0009-4000-a000-000000000001', 5, 'What blows your mind most? Comment! 🤯', 'mind blown neon', 4);

-- ── Video 10: Human Brain ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('30000001-0010-4000-a000-000000000001', 'b2c3d4e5-f6a7-8901-bcde-f12345678901',
        'Your Brain Uses More Energy Than a Light Bulb 💡🧠', 'human brain amazing facts quiz',
        'Your brain uses more energy than you think. But how much exactly?',
        'Follow @BrainBuzzQuiz — new quiz every day! 🧠', '#brain,#science,#facts,#quiz,#energy',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('40000010-0001-4000-a000-000000000001', '30000001-0010-4000-a000-000000000001', 0, 'How much of your body energy does your BRAIN use? 🧠', 'brain with electricity sparks', 5),
('40000010-0001-4000-a000-000000000002', '30000001-0010-4000-a000-000000000001', 1, 'A) 5%  B) 10%  C) 20%  D) 40%', 'percentage pie chart options', 6),
('40000010-0001-4000-a000-000000000003', '30000001-0010-4000-a000-000000000001', 2, 'Final answer? ⏰ 3... 2... 1...', 'dramatic countdown', 3),
('40000010-0001-4000-a000-000000000004', '30000001-0010-4000-a000-000000000001', 3, '✅ C) 20%! Your brain is only 2% of body weight but uses 20% of energy', 'brain glowing with energy', 6),
('40000010-0001-4000-a000-000000000005', '30000001-0010-4000-a000-000000000001', 4, 'Fun fact: Your brain generates enough electricity to power a small light bulb! 💡', 'light bulb turning on from brain', 5),
('40000010-0001-4000-a000-000000000006', '30000001-0010-4000-a000-000000000001', 5, 'How many did you get right today? Score below! 👇', 'scoreboard prompt', 4);
