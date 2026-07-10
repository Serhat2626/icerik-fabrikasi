-- V4: Shorts script tabloları + Money Mindset ilk 10 video script

CREATE TABLE shorts_scripts (
    id                       UUID PRIMARY KEY,
    channel_id               UUID NOT NULL REFERENCES channels(id),
    title                    VARCHAR(200) NOT NULL,
    topic                    VARCHAR(500) NOT NULL,
    hook                     VARCHAR(500) NOT NULL,
    cta                      VARCHAR(300),
    hashtags                 VARCHAR(500),
    status                   VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    target_duration_seconds  INTEGER NOT NULL DEFAULT 45,
    review_notes             TEXT,
    approved_at              TIMESTAMP,
    published_at             TIMESTAMP,
    render_output_path       VARCHAR(500),
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP
);

CREATE TABLE shorts_script_scenes (
    id                       UUID PRIMARY KEY,
    shorts_script_id         UUID NOT NULL REFERENCES shorts_scripts(id) ON DELETE CASCADE,
    order_index              INTEGER NOT NULL,
    text                     VARCHAR(500) NOT NULL,
    visual_hint              VARCHAR(500),
    duration_seconds         INTEGER NOT NULL DEFAULT 5,
    created_at               TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMP
);

CREATE INDEX idx_shorts_scripts_channel ON shorts_scripts(channel_id);
CREATE INDEX idx_shorts_scripts_status ON shorts_scripts(status);
CREATE INDEX idx_shorts_scenes_script ON shorts_script_scenes(shorts_script_id);

-- ═══════════════════════════════════════════════════
-- SEED DATA: Money Mindset — İlk 10 Video Script
-- ═══════════════════════════════════════════════════

-- ── Video 1: 5 Money Rules ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0001-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        '5 Money Rules The Rich Never Break 💰', '5 money rules rich people follow',
        'Rich people follow these 5 rules. Most people break all of them.',
        'Follow for more money wisdom 💰', '#money,#finance,#wealth,#richlife,#moneyrules',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000001-0001-4000-a000-000000000001', '10000001-0001-4000-a000-000000000001', 0, 'Rich people follow 5 rules most ignore', 'luxury lifestyle overview', 5),
('20000001-0001-4000-a000-000000000002', '10000001-0001-4000-a000-000000000001', 1, 'Rule 1: Pay yourself first — before any bill', 'person putting money in savings jar', 6),
('20000001-0001-4000-a000-000000000003', '10000001-0001-4000-a000-000000000001', 2, 'Rule 2: Never buy liabilities with debt', 'credit card being cut', 6),
('20000001-0001-4000-a000-000000000004', '10000001-0001-4000-a000-000000000001', 3, 'Rule 3: Invest before you feel ready', 'stock market charts going up', 6),
('20000001-0001-4000-a000-000000000005', '10000001-0001-4000-a000-000000000001', 4, 'Rule 4: Multiple income streams — never just one', 'multiple revenue streams diagram', 6),
('20000001-0001-4000-a000-000000000006', '10000001-0001-4000-a000-000000000001', 5, 'Rule 5: Live below your means — even when rich', 'modest home vs mansion comparison', 6),
('20000001-0001-4000-a000-000000000007', '10000001-0001-4000-a000-000000000001', 6, 'Break these rules and money will always slip away', 'money flying away animation', 5),
('20000001-0001-4000-a000-000000000008', '10000001-0001-4000-a000-000000000001', 7, 'Which rule do you break most? Comment below 👇', 'engagement prompt graphic', 5);

-- ── Video 2: Why 90% Stay Broke ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0002-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'Why 90% of People Will Never Be Rich 🚫', 'why most people stay broke',
        'Want to know why 90% of people will never be rich? It is not what you think.',
        'Save this for later ⭐', '#money,#mindset,#wealth,#financialfreedom,#broke',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000002-0001-4000-a000-000000000001', '10000001-0002-4000-a000-000000000001', 0, '90% of people will never build real wealth', 'crowded street with people rushing', 5),
('20000002-0001-4000-a000-000000000002', '10000001-0002-4000-a000-000000000001', 1, 'Not because they earn too little', 'paycheck closeup', 5),
('20000002-0001-4000-a000-000000000003', '10000001-0002-4000-a000-000000000001', 2, 'But because they spend to impress others', 'luxury car in front of rented apartment', 6),
('20000002-0001-4000-a000-000000000004', '10000001-0002-4000-a000-000000000001', 3, 'They trade time for money instead of building assets', 'clock turning into dollar bills', 6),
('20000002-0001-4000-a000-000000000005', '10000001-0002-4000-a000-000000000001', 4, 'They follow the crowd instead of learning finance', 'sheep following each other', 6),
('20000002-0001-4000-a000-000000000006', '10000001-0002-4000-a000-000000000001', 5, 'They wait for the perfect moment that never comes', 'person hesitating at a crossroad', 6),
('20000002-0001-4000-a000-000000000007', '10000001-0002-4000-a000-000000000001', 6, 'The top 10% simply decided to start', 'confident person walking forward', 6),
('20000002-0001-4000-a000-000000000008', '10000001-0002-4000-a000-000000000001', 7, 'Which group are you in? Be honest 💭', 'mirror reflection', 5);

-- ── Video 3: The $100 Rule ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0003-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'The $100 Rule That Changed My Life 💵', 'the 100 dollar rule for spending decisions',
        'This $100 rule will completely change how you spend money.',
        'Follow for more smart money tips 🧠', '#money,#spending,#savemoney,#moneytips,#budgeting',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000003-0001-4000-a000-000000000001', '10000001-0003-4000-a000-000000000001', 0, 'The $100 Rule changed everything for me', 'person looking at price tag', 5),
('20000003-0001-4000-a000-000000000002', '10000001-0003-4000-a000-000000000001', 1, 'Before buying anything — divide the price by $100', 'calculator showing division', 6),
('20000003-0001-4000-a000-000000000003', '10000001-0003-4000-a000-000000000001', 2, 'A $300 jacket? That is 3 days of work', 'jacket with price tag overlay', 5),
('20000003-0001-4000-a000-000000000004', '10000001-0003-4000-a000-000000000001', 3, 'A $1200 phone? 12 days of your life', 'smartphone with calendar overlay', 5),
('20000003-0001-4000-a000-000000000005', '10000001-0003-4000-a000-000000000001', 4, 'A $50K car? 500 days — over a year of work', 'luxury car fading away', 6),
('20000003-0001-4000-a000-000000000006', '10000001-0003-4000-a000-000000000001', 5, 'Now ask: is it worth that many days of my life?', 'person thinking deeply', 6),
('20000003-0001-4000-a000-000000000007', '10000001-0003-4000-a000-000000000001', 6, 'This simple math will save you thousands 💰', 'growing savings chart', 5);

-- ── Video 4: Stop Saving Money ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0004-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'Stop Saving Money — Do This Instead 🔥', 'why saving money alone wont make you rich',
        'Saving money is actually making you poorer. Here is what to do instead.',
        'Share this with someone who needs to hear it 🔥', '#investing,#money,#savings,#wealth,#inflation',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000004-0001-4000-a000-000000000001', '10000001-0004-4000-a000-000000000001', 0, 'Stop saving money. Seriously.', 'piggy bank being smashed', 5),
('20000004-0001-4000-a000-000000000002', '10000001-0004-4000-a000-000000000001', 1, 'Inflation eats 3-7% of your savings every year', 'money shrinking animation', 6),
('20000004-0001-4000-a000-000000000003', '10000001-0004-4000-a000-000000000001', 2, '$10K in savings today = $7K purchasing power in 5 years', 'declining graph', 6),
('20000004-0001-4000-a000-000000000004', '10000001-0004-4000-a000-000000000001', 3, 'Instead: invest in assets that grow faster than inflation', 'growing investment portfolio', 6),
('20000004-0001-4000-a000-000000000005', '10000001-0004-4000-a000-000000000001', 4, 'Index funds historically return 8-12% per year', 'S&P 500 chart going up', 6),
('20000004-0001-4000-a000-000000000006', '10000001-0004-4000-a000-000000000001', 5, 'Your money should work harder than you do', 'money with muscles lifting weights', 5),
('20000004-0001-4000-a000-000000000007', '10000001-0004-4000-a000-000000000001', 6, 'Keep 3-6 months emergency fund — invest the rest', 'balance between safety and growth', 6),
('20000004-0001-4000-a000-000000000008', '10000001-0004-4000-a000-000000000001', 7, 'Are you saving or investing? Big difference 💡', 'comparison infographic', 5);

-- ── Video 5: Things Poor People Buy ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0005-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        '3 Things Poor People Buy That Rich People Avoid 🚫💰', 'spending habits that keep people poor',
        'Rich people never buy these 3 things. But broke people buy them every single day.',
        'Follow for the mindset shift 🧠', '#money,#richvsrich,#spending,#financetips,#wealthmindset',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000005-0001-4000-a000-000000000001', '10000001-0005-4000-a000-000000000001', 0, 'These 3 purchases keep people broke forever', 'empty wallet closeup', 5),
('20000005-0001-4000-a000-000000000002', '10000001-0005-4000-a000-000000000001', 1, '1. Brand new cars — they lose 20% value instantly', 'new car driving off dealership lot', 6),
('20000005-0001-4000-a000-000000000003', '10000001-0005-4000-a000-000000000001', 2, 'Rich people buy used or lease strategically', 'modest reliable car', 5),
('20000005-0001-4000-a000-000000000004', '10000001-0005-4000-a000-000000000001', 3, '2. Fast fashion — cheap clothes you replace every month', 'overflowing closet', 6),
('20000005-0001-4000-a000-000000000005', '10000001-0005-4000-a000-000000000001', 4, 'Rich people buy fewer items but higher quality', 'minimalist wardrobe', 5),
('20000005-0001-4000-a000-000000000006', '10000001-0005-4000-a000-000000000001', 5, '3. Lottery tickets — a tax on people bad at math', 'lottery tickets in trash', 6),
('20000005-0001-4000-a000-000000000007', '10000001-0005-4000-a000-000000000001', 6, 'Rich people invest that same money in index funds', 'growing investment chart', 6),
('20000005-0001-4000-a000-000000000008', '10000001-0005-4000-a000-000000000001', 7, 'Stop buying things that steal your future 🔥', 'person walking toward sunrise', 5);

-- ── Video 6: 50/30/20 Rule ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0006-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'The 50/30/20 Rule Explained in 45 Seconds ⚡', 'the 50 30 20 budgeting rule explained simply',
        'If you can not manage $1000 you will never manage $100K. Use this rule.',
        'Save this and start today ⭐', '#budgeting,#money,#503020,#personalfinance,#moneytips',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000006-0001-4000-a000-000000000001', '10000001-0006-4000-a000-000000000001', 0, 'The simplest budget rule that actually works', 'clean budget spreadsheet', 5),
('20000006-0001-4000-a000-000000000002', '10000001-0006-4000-a000-000000000001', 1, '50% of income → needs: rent, food, bills', 'house and groceries icons', 6),
('20000006-0001-4000-a000-000000000003', '10000001-0006-4000-a000-000000000001', 2, '30% of income → wants: fun, dining, entertainment', 'restaurant and movie icons', 6),
('20000006-0001-4000-a000-000000000004', '10000001-0006-4000-a000-000000000001', 3, '20% of income → savings and investments', 'growing money tree', 6),
('20000006-0001-4000-a000-000000000005', '10000001-0006-4000-a000-000000000001', 4, 'Earn $4000? $2000 needs, $1200 wants, $800 investing', 'pie chart breakdown', 6),
('20000006-0001-4000-a000-000000000006', '10000001-0006-4000-a000-000000000001', 5, 'The secret? Automate it — set it and forget it', 'automatic transfer setup', 5),
('20000006-0001-4000-a000-000000000007', '10000001-0006-4000-a000-000000000001', 6, 'In 5 years you will thank yourself 🚀', 'future wealthy self', 5);

-- ── Video 7: Compound Interest ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0007-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'This Math Trick Makes Millionaires 📈', 'compound interest explained simply',
        'Einstein called this the 8th wonder of the world. And it can make you a millionaire.',
        'Follow to learn money math 📊', '#compoundinterest,#investing,#millionaire,#money,#math',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000007-0001-4000-a000-000000000001', '10000001-0007-4000-a000-000000000001', 0, 'The math trick that creates millionaires', 'einstein quote overlay', 5),
('20000007-0001-4000-a000-000000000002', '10000001-0007-4000-a000-000000000001', 1, 'Invest $200/month starting at age 25', 'young person setting up investment', 6),
('20000007-0001-4000-a000-000000000003', '10000001-0007-4000-a000-000000000001', 2, 'At 10% return — by 65 you have $1.2 MILLION', 'compound growth chart exploding up', 6),
('20000007-0001-4000-a000-000000000004', '10000001-0007-4000-a000-000000000001', 3, 'You only invested $96K — the rest is compound interest', 'small amount growing into mountain', 6),
('20000007-0001-4000-a000-000000000005', '10000001-0007-4000-a000-000000000001', 4, 'Start at 35? You only get $400K with the same amount', 'shorter growth chart', 6),
('20000007-0001-4000-a000-000000000006', '10000001-0007-4000-a000-000000000001', 5, 'Every year you wait costs you $100K+', 'money clock ticking', 5),
('20000007-0001-4000-a000-000000000007', '10000001-0007-4000-a000-000000000001', 6, 'The best time to start was yesterday. The next best is NOW', 'start button being pressed', 5);

-- ── Video 8: Side Hustle Mindset ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0008-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'Your 9-5 Will Never Make You Rich 💼➡️💰', 'why a single income stream is risky',
        'Your job pays your bills. But it will never make you wealthy. Here is why.',
        'Double tap if you agree ❤️', '#sidehustle,#money,#passiveincome,#entrepreneur,#wealth',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000008-0001-4000-a000-000000000001', '10000001-0008-4000-a000-000000000001', 0, 'Your salary has a ceiling. Your side income does not.', 'office ceiling with cap', 5),
('20000008-0001-4000-a000-000000000002', '10000001-0008-4000-a000-000000000001', 1, 'Average millionaire has 7 income streams', '7 rivers flowing into one lake', 6),
('20000008-0001-4000-a000-000000000003', '10000001-0008-4000-a000-000000000001', 2, 'You only need to start with ONE extra stream', 'single river starting to flow', 5),
('20000008-0001-4000-a000-000000000004', '10000001-0008-4000-a000-000000000001', 3, 'Digital products cost $0 to create and sell 24/7', 'laptop showing online store', 6),
('20000008-0001-4000-a000-000000000005', '10000001-0008-4000-a000-000000000001', 4, 'Content creation builds audience + income together', 'growing social media followers', 6),
('20000008-0001-4000-a000-000000000006', '10000001-0008-4000-a000-000000000001', 5, 'Invest your side income — now money makes money', 'money reinvestment cycle', 6),
('20000008-0001-4000-a000-000000000007', '10000001-0008-4000-a000-000000000001', 6, 'Start small. Stay consistent. Scale later.', 'growth staircase', 5),
('20000008-0001-4000-a000-000000000008', '10000001-0008-4000-a000-000000000001', 7, 'What is your side hustle? Drop it below 👇', 'comment prompt', 5);

-- ── Video 9: Money Mistakes in Your 20s ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0009-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        '5 Money Mistakes That Ruin Your 20s 😱', 'biggest financial mistakes young people make',
        'I wish someone told me this at 20. These 5 mistakes cost me years.',
        'Send this to someone in their 20s 📩', '#money,#20s,#mistakes,#youngadult,#financetips',
        'DRAFT', 45);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000009-0001-4000-a000-000000000001', '10000001-0009-4000-a000-000000000001', 0, 'Your 20s can set you up for life — or ruin it', 'crossroads at age 20', 5),
('20000009-0001-4000-a000-000000000002', '10000001-0009-4000-a000-000000000001', 1, 'Mistake 1: No emergency fund — one crisis destroys you', 'broken down car with no money', 6),
('20000009-0001-4000-a000-000000000003', '10000001-0009-4000-a000-000000000001', 2, 'Mistake 2: Lifestyle inflation — earn more, spend more', 'paycheck vanishing instantly', 6),
('20000009-0001-4000-a000-000000000004', '10000001-0009-4000-a000-000000000001', 3, 'Mistake 3: Ignoring credit score — it follows you everywhere', 'bad credit score on screen', 6),
('20000009-0001-4000-a000-000000000005', '10000001-0009-4000-a000-000000000001', 4, 'Mistake 4: Not investing early — you lose compound time', 'empty hourglass', 5),
('20000009-0001-4000-a000-000000000006', '10000001-0009-4000-a000-000000000001', 5, 'Mistake 5: Buying things to impress people you do not like', 'fake social media lifestyle', 6),
('20000009-0001-4000-a000-000000000007', '10000001-0009-4000-a000-000000000001', 6, 'Fix these now and your 30s will be completely different', 'thriving 30-year-old', 5);

-- ── Video 10: The Latte Factor ──
INSERT INTO shorts_scripts (id, channel_id, title, topic, hook, cta, hashtags, status, target_duration_seconds)
VALUES ('10000001-0010-4000-a000-000000000001', 'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
        'Your Coffee Habit Costs You $100K ☕💸', 'the latte factor and small daily expenses',
        'Your daily coffee is stealing $100K from your future. Let me show you the math.',
        'Follow for eye-opening money facts 👁️', '#coffee,#money,#lattefactor,#saving,#investing',
        'DRAFT', 40);
INSERT INTO shorts_script_scenes (id, shorts_script_id, order_index, text, visual_hint, duration_seconds) VALUES
('20000010-0001-4000-a000-000000000001', '10000001-0010-4000-a000-000000000001', 0, 'That $5 coffee is more expensive than you think', 'coffee cup with dollar signs', 5),
('20000010-0001-4000-a000-000000000002', '10000001-0010-4000-a000-000000000001', 1, '$5/day × 365 days = $1,825 per year', 'calendar with money flying out', 5),
('20000010-0001-4000-a000-000000000003', '10000001-0010-4000-a000-000000000001', 2, 'Invest that $1,825 yearly at 10% return', 'investment account deposit', 5),
('20000010-0001-4000-a000-000000000004', '10000001-0010-4000-a000-000000000001', 3, 'In 10 years: $31,000', '10 year milestone', 5),
('20000010-0001-4000-a000-000000000005', '10000001-0010-4000-a000-000000000001', 4, 'In 20 years: $115,000', '20 year milestone with bigger number', 5),
('20000010-0001-4000-a000-000000000006', '10000001-0010-4000-a000-000000000001', 5, 'In 30 years: $340,000 — from just coffee money', '30 year milestone exploding', 5),
('20000010-0001-4000-a000-000000000007', '10000001-0010-4000-a000-000000000001', 6, 'Not saying quit coffee — just know what it really costs', 'balanced perspective scale', 5),
('20000010-0001-4000-a000-000000000008', '10000001-0010-4000-a000-000000000001', 7, 'What small expense would you cut first? 👇', 'comment engagement prompt', 5);
