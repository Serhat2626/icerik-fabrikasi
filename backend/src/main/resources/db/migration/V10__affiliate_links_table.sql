-- =====================================================================
-- V10: Affiliate Link Yönetim Tablosu
-- =====================================================================

CREATE TABLE IF NOT EXISTS affiliate_links (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    channel_id UUID NOT NULL REFERENCES channels(id) ON DELETE CASCADE,
    product_name VARCHAR(100) NOT NULL,
    affiliate_url VARCHAR(500) NOT NULL,
    commission_type VARCHAR(20) NOT NULL DEFAULT 'ONE_TIME',
    commission_value VARCHAR(50),
    display_text VARCHAR(200) NOT NULL,
    keywords VARCHAR(500),
    priority INTEGER NOT NULL DEFAULT 5,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    emoji VARCHAR(10) DEFAULT '🔗'
);

CREATE INDEX idx_affiliate_links_channel_active ON affiliate_links(channel_id, active);

-- Channels tablosuna açıklama template alanı ekle
ALTER TABLE channels ADD COLUMN IF NOT EXISTS description_template TEXT;
