-- =====================================================================
-- V13: Money Mind Affiliate Linkler
-- =====================================================================
-- 💰 Money Mind kanalı V9'da oluşturuldu, burada affiliate linkleri ekleniyor

WITH mm_channel AS (
    SELECT id FROM channels WHERE name = 'Money Mind' LIMIT 1
)
INSERT INTO affiliate_links (id, created_at, updated_at, channel_id, product_name, affiliate_url, commission_type, commission_value, display_text, keywords, priority, active, emoji)
SELECT
    gen_random_uuid(), NOW(), NOW(), mc.id,
    v.product_name, v.affiliate_url, v.commission_type, v.commission_value,
    v.display_text, v.keywords, v.priority, TRUE, v.emoji
FROM mm_channel mc,
(VALUES
    ('Wise', 'https://wise.com/invite/moneymind', 'CPA', '$10-30', 'Send money worldwide FREE — Wise', 'money,transfer,send,international,bank,currency,wise,exchange', 1, '💸'),
    ('Revolut', 'https://revolut.com/ref/moneymind', 'CPA', '€10-25', 'Smart banking with Revolut', 'bank,card,spend,save,revolut,account,fintech', 2, '💳'),
    ('NordVPN', 'https://nordvpn.com/moneymind', 'REVENUE_SHARE', '40-100% + 30% renewal', 'Protect your financial data — NordVPN', 'security,privacy,vpn,protect,data,online,safe', 3, '🔒'),
    ('Acorns', 'https://acorns.com/invite/moneymind', 'ONE_TIME', '$5-20', 'Start investing spare change — Acorns', 'invest,investing,stock,save,money,grow,wealth,portfolio', 4, '🌱'),
    ('Wealthfront', 'https://wealthfront.com/moneymind', 'ONE_TIME', '$50-75', 'Automated investing with Wealthfront', 'invest,robo,advisor,portfolio,retire,wealth,compound', 5, '📈'),
    ('Trading 212', 'https://trading212.com/invite/moneymind', 'CPA', 'Free share', 'Get a FREE stock — Trading 212', 'stock,trade,share,free,invest,market,trading', 6, '📊')
) AS v(product_name, affiliate_url, commission_type, commission_value, display_text, keywords, priority, emoji);
