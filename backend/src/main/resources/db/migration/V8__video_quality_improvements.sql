-- =====================================================================
-- V8: Video Kalite İyileştirmeleri — Kanal Tablosuna Yeni Alanlar
-- =====================================================================
-- Kanal bazlı ses, görsel ve geçiş konfigürasyonu alanları ekleniyor.

-- Ses konfigürasyonu
ALTER TABLE channels ADD COLUMN IF NOT EXISTS voice_id VARCHAR(50);
ALTER TABLE channels ADD COLUMN IF NOT EXISTS voice_model VARCHAR(50);
ALTER TABLE channels ADD COLUMN IF NOT EXISTS voice_stability DOUBLE PRECISION;
ALTER TABLE channels ADD COLUMN IF NOT EXISTS voice_similarity_boost DOUBLE PRECISION;
ALTER TABLE channels ADD COLUMN IF NOT EXISTS voice_style DOUBLE PRECISION;

-- Görsel üretim konfigürasyonu
ALTER TABLE channels ADD COLUMN IF NOT EXISTS image_style_preset TEXT;
ALTER TABLE channels ADD COLUMN IF NOT EXISTS hook_font_size INTEGER DEFAULT 96;
ALTER TABLE channels ADD COLUMN IF NOT EXISTS body_font_size INTEGER DEFAULT 72;
ALTER TABLE channels ADD COLUMN IF NOT EXISTS transition_type VARCHAR(20);

-- Mevcut kanalların font boyutlarını güncelle (0 olanları varsayılana çek)
UPDATE channels SET hook_font_size = 96 WHERE hook_font_size IS NULL OR hook_font_size = 0;
UPDATE channels SET body_font_size = 72 WHERE body_font_size IS NULL OR body_font_size = 0;
