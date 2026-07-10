-- V7: knowledge_items tablosuna eksik kolonlar ekleniyor
-- Entity'de var ama V2 migration'da tanımlanmamış kolonlar
ALTER TABLE knowledge_items ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE knowledge_items ADD COLUMN IF NOT EXISTS category VARCHAR(100);
ALTER TABLE knowledge_items ADD COLUMN IF NOT EXISTS city VARCHAR(100);
ALTER TABLE knowledge_items ADD COLUMN IF NOT EXISTS district VARCHAR(100);
ALTER TABLE knowledge_items ADD COLUMN IF NOT EXISTS location_text VARCHAR(500);
ALTER TABLE knowledge_items ADD COLUMN IF NOT EXISTS notes TEXT;
