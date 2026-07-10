-- ============================================================
-- İçerik Fabrikası — V2: İlk Domain Tabloları
-- ============================================================
-- Kapsam: Knowledge, Content, AI, Media domain tabloları
-- ============================================================

-- ============================================================
-- 1. KNOWLEDGE DOMAIN
-- ============================================================

CREATE TABLE knowledge_items (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    type VARCHAR(50) NOT NULL,
    summary TEXT,
    status VARCHAR(50) NOT NULL,
    language VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE source_references (
    id UUID PRIMARY KEY,
    knowledge_item_id UUID NOT NULL,
    title VARCHAR(500) NOT NULL,
    url TEXT,
    author VARCHAR(255),
    source_type VARCHAR(50) NOT NULL,
    reliability_level VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_source_references_knowledge_item
        FOREIGN KEY (knowledge_item_id)
        REFERENCES knowledge_items(id)
        ON DELETE CASCADE
);

CREATE TABLE facts (
    id UUID PRIMARY KEY,
    knowledge_item_id UUID NOT NULL,
    source_reference_id UUID,
    statement TEXT NOT NULL,
    verification_status VARCHAR(50) NOT NULL,
    notes TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_facts_knowledge_item
        FOREIGN KEY (knowledge_item_id)
        REFERENCES knowledge_items(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_facts_source_reference
        FOREIGN KEY (source_reference_id)
        REFERENCES source_references(id)
        ON DELETE SET NULL
);

-- ============================================================
-- 2. CONTENT DOMAIN
-- ============================================================

CREATE TABLE content_projects (
    id UUID PRIMARY KEY,
    knowledge_item_id UUID NOT NULL,
    title VARCHAR(255),
    slug VARCHAR(255),
    content_type VARCHAR(50),
    target_platform VARCHAR(50),
    status VARCHAR(50) NOT NULL,
    language VARCHAR(10) NOT NULL,
    content_purpose VARCHAR(50),
    target_audience VARCHAR(50),
    tone VARCHAR(50),
    format_hint VARCHAR(255),
    length_hint VARCHAR(255),
    retry_count INTEGER NOT NULL DEFAULT 0,
    last_error_type VARCHAR(50),
    last_error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_content_projects_knowledge_item
        FOREIGN KEY (knowledge_item_id)
        REFERENCES knowledge_items(id)
        ON DELETE RESTRICT
);

CREATE TABLE script_drafts (
    id UUID PRIMARY KEY,
    content_project_id UUID NOT NULL,
    version INTEGER NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_script_drafts_content_project
        FOREIGN KEY (content_project_id)
        REFERENCES content_projects(id)
        ON DELETE CASCADE,
    CONSTRAINT uq_script_drafts_project_version
        UNIQUE (content_project_id, version)
);

CREATE TABLE content_drafts (
    id UUID PRIMARY KEY,
    content_project_id UUID NOT NULL,
    title VARCHAR(500),
    body TEXT,
    summary TEXT,
    status VARCHAR(50) NOT NULL,
    generated_by VARCHAR(50) NOT NULL,
    warnings_json TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_content_drafts_content_project
        FOREIGN KEY (content_project_id)
        REFERENCES content_projects(id)
        ON DELETE CASCADE
);

-- ============================================================
-- 3. AI PLATFORM
-- ============================================================

CREATE TABLE prompt_templates (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(50) NOT NULL,
    version INTEGER NOT NULL,
    content TEXT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT uq_prompt_template_name_version
        UNIQUE (name, version)
);

CREATE TABLE ai_generation_logs (
    id UUID PRIMARY KEY,
    content_project_id UUID,
    prompt_template_id UUID,
    provider VARCHAR(100) NOT NULL,
    model VARCHAR(100) NOT NULL,
    input_summary TEXT,
    output_summary TEXT,
    cost NUMERIC(12, 6),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_ai_generation_logs_content_project
        FOREIGN KEY (content_project_id)
        REFERENCES content_projects(id)
        ON DELETE SET NULL,
    CONSTRAINT fk_ai_generation_logs_prompt_template
        FOREIGN KEY (prompt_template_id)
        REFERENCES prompt_templates(id)
        ON DELETE SET NULL
);

CREATE TABLE ai_generation_requests (
    id UUID PRIMARY KEY,
    content_project_id UUID NOT NULL,
    attempt_number INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL,
    request_payload_json TEXT,
    response_payload_json TEXT,
    error_type VARCHAR(50),
    error_message TEXT,
    provider_name VARCHAR(100),
    model_name VARCHAR(100),
    duration_ms BIGINT,
    created_at TIMESTAMP NOT NULL,
    completed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_ai_gen_requests_content_project
        FOREIGN KEY (content_project_id)
        REFERENCES content_projects(id)
        ON DELETE CASCADE
);

-- ============================================================
-- 4. MEDIA DOMAIN
-- ============================================================

CREATE TABLE media_assets (
    id UUID PRIMARY KEY,
    asset_type VARCHAR(50) NOT NULL,
    storage_provider VARCHAR(50) NOT NULL,
    bucket VARCHAR(255) NOT NULL,
    object_key TEXT NOT NULL,
    file_name VARCHAR(500),
    mime_type VARCHAR(255),
    size_bytes BIGINT,
    duration_seconds INTEGER,
    metadata_json TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

CREATE TABLE generated_assets (
    id UUID PRIMARY KEY,
    media_asset_id UUID NOT NULL,
    content_project_id UUID,
    generator_type VARCHAR(50) NOT NULL,
    prompt TEXT,
    model VARCHAR(100),
    generation_status VARCHAR(50) NOT NULL,
    cost NUMERIC(12, 6),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    CONSTRAINT fk_generated_assets_media_asset
        FOREIGN KEY (media_asset_id)
        REFERENCES media_assets(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_generated_assets_content_project
        FOREIGN KEY (content_project_id)
        REFERENCES content_projects(id)
        ON DELETE SET NULL
);

-- ============================================================
-- 5. SCENARIO DOMAIN
-- ============================================================

CREATE TABLE scenario_drafts (
    id                          UUID PRIMARY KEY,
    content_project_id          UUID NOT NULL,
    source_draft_id             UUID NOT NULL,
    version                     INTEGER NOT NULL,
    status                      VARCHAR(50) NOT NULL,
    title                       VARCHAR(500),
    summary                     TEXT,
    target_audience             VARCHAR(100),
    cultural_objective          TEXT,
    tone                        VARCHAR(100),
    estimated_duration_seconds  INTEGER,
    setting                     VARCHAR(500),
    characters                  TEXT,
    review_notes                TEXT,
    generated_by                VARCHAR(50) NOT NULL,
    provider_type               VARCHAR(50),
    model_name                  VARCHAR(100),
    warnings_json               TEXT,
    created_at                  TIMESTAMP NOT NULL,
    updated_at                  TIMESTAMP,
    approved_at                 TIMESTAMP,
    rejected_at                 TIMESTAMP,
    edited_at                   TIMESTAMP,
    created_by                  VARCHAR(100),
    approved_by                 VARCHAR(100),
    rejected_by                 VARCHAR(100),
    edited_by                   VARCHAR(100),
    CONSTRAINT fk_scenario_drafts_content_project
        FOREIGN KEY (content_project_id)
        REFERENCES content_projects(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_scenario_drafts_source_draft
        FOREIGN KEY (source_draft_id)
        REFERENCES content_drafts(id)
        ON DELETE RESTRICT
);

CREATE TABLE scenario_beats (
    id                          UUID PRIMARY KEY,
    scenario_draft_id           UUID NOT NULL,
    type                        VARCHAR(50) NOT NULL,
    label                       VARCHAR(100) NOT NULL,
    body                        TEXT NOT NULL,
    order_index                 INTEGER NOT NULL,
    estimated_duration_seconds  INTEGER,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_scenario_beats_scenario_draft
        FOREIGN KEY (scenario_draft_id)
        REFERENCES scenario_drafts(id)
        ON DELETE CASCADE
);

-- ============================================================
-- 6. STORYBOARD DOMAIN
-- ============================================================

CREATE TABLE storyboard_drafts (
    id                          UUID PRIMARY KEY,
    content_project_id          UUID NOT NULL,
    source_draft_id             UUID NOT NULL,
    source_scenario_draft_id    UUID NOT NULL,
    source_scenario_version     INTEGER NOT NULL,
    version                     INTEGER NOT NULL,
    status                      VARCHAR(50) NOT NULL,
    title                       VARCHAR(500),
    summary                     TEXT,
    target_platform             VARCHAR(100),
    aspect_ratio                VARCHAR(20),
    estimated_duration_seconds  INTEGER,
    visual_style_guide          TEXT,
    narration_style             VARCHAR(500),
    review_notes                TEXT,
    generated_by                VARCHAR(50) NOT NULL,
    provider_type               VARCHAR(50),
    model_name                  VARCHAR(100),
    created_at                  TIMESTAMP NOT NULL,
    updated_at                  TIMESTAMP,
    approved_at                 TIMESTAMP,
    rejected_at                 TIMESTAMP,
    edited_at                   TIMESTAMP,
    created_by                  VARCHAR(100),
    approved_by                 VARCHAR(100),
    rejected_by                 VARCHAR(100),
    edited_by                   VARCHAR(100),
    CONSTRAINT fk_storyboard_drafts_content_project
        FOREIGN KEY (content_project_id)
        REFERENCES content_projects(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_storyboard_drafts_source_draft
        FOREIGN KEY (source_draft_id)
        REFERENCES content_drafts(id)
        ON DELETE RESTRICT,
    CONSTRAINT fk_storyboard_drafts_source_scenario
        FOREIGN KEY (source_scenario_draft_id)
        REFERENCES scenario_drafts(id)
        ON DELETE RESTRICT
);

CREATE TABLE storyboard_scenes (
    id                          UUID PRIMARY KEY,
    storyboard_draft_id         UUID NOT NULL,
    scene_number                INTEGER NOT NULL,
    source_beat_type            VARCHAR(50) NOT NULL,
    source_beat_order           INTEGER NOT NULL,
    scene_title                 VARCHAR(500) NOT NULL,
    visual_description          TEXT NOT NULL,
    narration_text              TEXT,
    on_screen_text              TEXT,
    camera_direction            VARCHAR(500),
    transition_note             VARCHAR(200),
    mood                        VARCHAR(200),
    location                    VARCHAR(500),
    characters                  TEXT,
    estimated_duration_seconds  INTEGER NOT NULL,
    media_generation_notes      TEXT,
    created_at                  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_storyboard_scenes_storyboard_draft
        FOREIGN KEY (storyboard_draft_id)
        REFERENCES storyboard_drafts(id)
        ON DELETE CASCADE
);

-- ============================================================
-- 7. PLATFORM PACKAGE DOMAIN
-- ============================================================

CREATE TABLE platform_package_drafts (
    id                              UUID PRIMARY KEY,
    content_project_id              UUID NOT NULL REFERENCES content_projects(id),
    source_storyboard_draft_id      UUID NOT NULL REFERENCES storyboard_drafts(id),
    package_type                    VARCHAR(30) NOT NULL,
    status                          VARCHAR(20) NOT NULL,
    version                         INTEGER NOT NULL DEFAULT 1,
    title                           VARCHAR(255) NOT NULL,
    caption_or_description          TEXT NOT NULL,
    hashtags_json                   TEXT,
    call_to_action                  VARCHAR(500),
    target_duration_seconds         INTEGER,
    aspect_ratio                    VARCHAR(10),
    content_structure_notes         TEXT,
    scene_usage_notes               TEXT,
    review_notes                    TEXT,
    video_title                     VARCHAR(255),
    long_description                TEXT,
    chapter_outline_json            TEXT,
    pinned_comment_suggestion       TEXT,
    thumbnail_text_suggestion       VARCHAR(100),
    tags_json                       TEXT,
    short_hook                      VARCHAR(500),
    short_caption                   TEXT,
    overlay_text_suggestions_json   TEXT,
    short_hashtag_set_json          TEXT,
    short_cta                       VARCHAR(500),
    duration_guidance               VARCHAR(100),
    provider_type                   VARCHAR(20),
    model_name                      VARCHAR(50),
    generated_by                    VARCHAR(30),
    edited_at                       TIMESTAMP,
    edited_by                       VARCHAR(100),
    approved_at                     TIMESTAMP,
    approved_by                     VARCHAR(100),
    rejected_at                     TIMESTAMP,
    rejected_by                     VARCHAR(100),
    rejection_reason                TEXT,
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE platform_package_scene_usages (
    id                              UUID PRIMARY KEY,
    platform_package_draft_id       UUID NOT NULL REFERENCES platform_package_drafts(id) ON DELETE CASCADE,
    scene_number                    INTEGER NOT NULL,
    source_storyboard_scene_id      UUID REFERENCES storyboard_scenes(id),
    usage_type                      VARCHAR(20) NOT NULL,
    included                        BOOLEAN NOT NULL DEFAULT true,
    adaptation_note                 TEXT,
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 8. VISUAL ASSET DOMAIN
-- ============================================================

CREATE TABLE visual_asset_plans (
    id                              UUID PRIMARY KEY,
    content_project_id              UUID NOT NULL REFERENCES content_projects(id),
    source_storyboard_draft_id      UUID NOT NULL REFERENCES storyboard_drafts(id),
    version                         INTEGER NOT NULL,
    status                          VARCHAR(20) NOT NULL,
    title                           VARCHAR(500),
    summary                         TEXT,
    overall_visual_style            TEXT,
    aspect_ratio_strategy           VARCHAR(100),
    cultural_authenticity_guidance   TEXT,
    character_continuity_guidance    TEXT,
    location_continuity_guidance    TEXT,
    color_palette_guidance          TEXT,
    review_notes                    TEXT,
    generated_by                    VARCHAR(50) NOT NULL,
    provider_type                   VARCHAR(50),
    model_name                      VARCHAR(100),
    approved_at                     TIMESTAMP,
    approved_by                     VARCHAR(100),
    rejected_at                     TIMESTAMP,
    rejected_by                     VARCHAR(100),
    rejection_reason                TEXT,
    edited_at                       TIMESTAMP,
    edited_by                       VARCHAR(100),
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE scene_visual_plans (
    id                              UUID PRIMARY KEY,
    visual_asset_plan_id            UUID NOT NULL REFERENCES visual_asset_plans(id) ON DELETE CASCADE,
    source_storyboard_scene_id      UUID REFERENCES storyboard_scenes(id),
    scene_number                    INTEGER NOT NULL,
    source_beat_type                VARCHAR(50),
    scene_title                     VARCHAR(500),
    english_image_prompt            TEXT NOT NULL,
    turkish_visual_context          TEXT NOT NULL,
    negative_prompt                 TEXT,
    style_directive                 VARCHAR(200),
    aspect_ratio                    VARCHAR(20),
    composition_notes               TEXT,
    camera_framing                  VARCHAR(200),
    lighting_notes                  TEXT,
    color_palette                   TEXT,
    mood                            VARCHAR(200),
    location                        TEXT,
    characters                      TEXT,
    cultural_details                TEXT,
    text_overlay_guidance           TEXT,
    image_generation_tool_notes     TEXT,
    safety_notes                    TEXT,
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE thumbnail_plans (
    id                              UUID PRIMARY KEY,
    visual_asset_plan_id            UUID NOT NULL UNIQUE REFERENCES visual_asset_plans(id) ON DELETE CASCADE,
    source_storyboard_scene_id      UUID REFERENCES storyboard_scenes(id),
    source_scene_number             INTEGER,
    thumbnail_prompt                TEXT NOT NULL,
    negative_prompt                 TEXT,
    text_overlay_guidance           TEXT,
    composition_notes               TEXT,
    emotional_hook                  VARCHAR(500),
    aspect_ratio                    VARCHAR(20),
    style_directive                 VARCHAR(200),
    color_palette                   TEXT,
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 9. IMAGE GENERATION DOMAIN
-- ============================================================

CREATE TABLE image_generation_requests (
    id                      UUID PRIMARY KEY,
    content_project_id      UUID NOT NULL REFERENCES content_projects(id),
    visual_asset_plan_id    UUID NOT NULL REFERENCES visual_asset_plans(id),
    visual_asset_item_id    UUID NOT NULL,
    item_type               VARCHAR(30)  NOT NULL,
    status                  VARCHAR(40)  NOT NULL,
    idempotency_key         VARCHAR(255) NOT NULL UNIQUE,
    provider_type           VARCHAR(50),
    model_name              VARCHAR(100),
    safe_prompt_summary     TEXT,
    error_code              VARCHAR(50),
    error_message           TEXT,
    correlation_id          UUID,
    requested_by            VARCHAR(100),
    requested_at            TIMESTAMP,
    completed_at            TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE image_generation_results (
    id                              UUID PRIMARY KEY,
    image_generation_request_id     UUID NOT NULL REFERENCES image_generation_requests(id),
    visual_asset_plan_id            UUID NOT NULL,
    visual_asset_item_id            UUID NOT NULL,
    provider_type                   VARCHAR(50),
    model_name                      VARCHAR(100),
    provider_request_id             VARCHAR(255),
    generation_duration_ms          BIGINT,
    output_count                    INTEGER,
    mime_type                       VARCHAR(50),
    width                           INTEGER,
    height                          INTEGER,
    file_size_bytes                 BIGINT,
    checksum_sha256                 VARCHAR(64),
    error_code                      VARCHAR(50),
    error_message                   TEXT,
    correlation_id                  UUID,
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fp007_image_media_assets (
    id                      UUID PRIMARY KEY,
    storage_provider        VARCHAR(50),
    storage_bucket          VARCHAR(255),
    storage_object_key      TEXT,
    original_file_name      VARCHAR(500),
    mime_type               VARCHAR(50),
    width                   INTEGER,
    height                  INTEGER,
    file_size_bytes         BIGINT,
    checksum_sha256         VARCHAR(64),
    asset_kind              VARCHAR(30) NOT NULL,
    review_status           VARCHAR(30),
    storage_handoff_status  VARCHAR(30),
    preview_status          VARCHAR(30),
    storage_error_code      VARCHAR(60),
    storage_error_message   TEXT,
    stored_at               TIMESTAMP,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fp007_generated_assets (
    id                              UUID PRIMARY KEY,
    image_generation_request_id     UUID NOT NULL REFERENCES image_generation_requests(id),
    image_generation_result_id      UUID REFERENCES image_generation_results(id),
    image_media_asset_id            UUID REFERENCES fp007_image_media_assets(id),
    content_project_id              UUID NOT NULL REFERENCES content_projects(id),
    visual_asset_plan_id            UUID NOT NULL REFERENCES visual_asset_plans(id),
    visual_asset_item_id            UUID NOT NULL,
    status                          VARCHAR(40) NOT NULL,
    safe_prompt_summary             TEXT,
    quality_signals_summary         TEXT,
    safety_flags_summary            TEXT,
    reviewed_by                     VARCHAR(100),
    reviewed_at                     TIMESTAMP,
    rejection_reason                TEXT,
    created_at                      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fp007_generated_asset_reviews (
    id                      UUID PRIMARY KEY,
    generated_asset_id      UUID NOT NULL REFERENCES fp007_generated_assets(id),
    decision                VARCHAR(20) NOT NULL,
    rejection_reason        TEXT,
    reviewed_by             VARCHAR(100) NOT NULL,
    reviewed_at             TIMESTAMP NOT NULL,
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 10. REVIEW DOMAIN
-- ============================================================

CREATE TABLE review_records (
    id                                  UUID PRIMARY KEY,
    review_target_type                  VARCHAR(50) NOT NULL,
    review_target_id                    UUID NOT NULL,
    current_state                       VARCHAR(50) NOT NULL,
    created_by_actor_reference          VARCHAR(100),
    current_reviewer_actor_reference    VARCHAR(100),
    last_action_type                    VARCHAR(50),
    last_action_at                      TIMESTAMP,
    last_action_by_actor_reference      VARCHAR(100),
    safe_reason_code                    VARCHAR(100),
    safe_message                        VARCHAR(2000),
    created_at                          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_rr_target UNIQUE (review_target_type, review_target_id)
);

CREATE TABLE review_audit_events (
    id                      UUID PRIMARY KEY,
    review_record_id        UUID NOT NULL,
    CONSTRAINT fk_rae_review_record FOREIGN KEY (review_record_id) REFERENCES review_records(id),
    event_type              VARCHAR(50) NOT NULL,
    from_state              VARCHAR(50),
    to_state                VARCHAR(50),
    action_type             VARCHAR(50),
    actor_role              VARCHAR(30),
    actor_reference         VARCHAR(100),
    occurred_at             TIMESTAMP NOT NULL,
    safe_reason_code        VARCHAR(100),
    safe_message            VARCHAR(2000),
    sanitized_note          VARCHAR(2000),
    created_at              TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- 11. AUTH DOMAIN
-- ============================================================

CREATE TABLE admin_users (
    id                  UUID PRIMARY KEY,
    username            VARCHAR(100) NOT NULL UNIQUE,
    password_hash       VARCHAR(255) NOT NULL,
    display_name        VARCHAR(200),
    role                VARCHAR(30) NOT NULL DEFAULT 'REVIEWER',
    enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    account_locked      BOOLEAN NOT NULL DEFAULT FALSE,
    last_login_at       TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Varsayılan admin kullanıcı (sadece dev/staging)
-- Şifre: IcerikFabrikasi.2026! (BCrypt hash)
INSERT INTO admin_users (id, username, password_hash, display_name, role, enabled)
VALUES (
    'a0000000-0000-0000-0000-000000000001',
    'admin',
    '$2a$10$x.npHQ3CScXSTLCKvdMwYuFlNgPCPYHg.0V5W4JPiKBzsTu1V99j2',
    'Sistem Yöneticisi',
    'ADMIN',
    true
);

-- ============================================================
-- 12. INDEXES
-- ============================================================

CREATE INDEX idx_knowledge_items_type ON knowledge_items(type);
CREATE INDEX idx_knowledge_items_status ON knowledge_items(status);
CREATE INDEX idx_content_projects_status ON content_projects(status);
CREATE INDEX idx_content_projects_platform ON content_projects(target_platform);
CREATE INDEX idx_script_drafts_content_project ON script_drafts(content_project_id);
CREATE INDEX idx_content_drafts_project_id ON content_drafts(content_project_id);
CREATE INDEX idx_content_drafts_status ON content_drafts(status);
CREATE INDEX idx_ai_gen_requests_project_id ON ai_generation_requests(content_project_id);
CREATE INDEX idx_ai_gen_requests_status ON ai_generation_requests(status);
CREATE INDEX idx_ai_generation_logs_content_project ON ai_generation_logs(content_project_id);
CREATE INDEX idx_media_assets_asset_type ON media_assets(asset_type);
CREATE INDEX idx_scenario_drafts_project_id ON scenario_drafts(content_project_id);
CREATE INDEX idx_scenario_drafts_status ON scenario_drafts(status);
CREATE INDEX idx_scenario_beats_draft_id ON scenario_beats(scenario_draft_id);
CREATE INDEX idx_storyboard_drafts_project_id ON storyboard_drafts(content_project_id);
CREATE INDEX idx_storyboard_drafts_status ON storyboard_drafts(status);
CREATE INDEX idx_storyboard_scenes_draft_id ON storyboard_scenes(storyboard_draft_id);
CREATE INDEX idx_ppd_content_project ON platform_package_drafts(content_project_id);
CREATE INDEX idx_ppd_status ON platform_package_drafts(status);
CREATE INDEX idx_ppsu_draft ON platform_package_scene_usages(platform_package_draft_id);
CREATE INDEX idx_vap_content_project ON visual_asset_plans(content_project_id);
CREATE INDEX idx_vap_status ON visual_asset_plans(status);
CREATE INDEX idx_svp_plan ON scene_visual_plans(visual_asset_plan_id);
CREATE INDEX idx_tp_plan ON thumbnail_plans(visual_asset_plan_id);
CREATE INDEX idx_igr_status ON image_generation_requests(status);
CREATE INDEX idx_igr_visual_asset_plan ON image_generation_requests(visual_asset_plan_id);
CREATE INDEX idx_igresult_request ON image_generation_results(image_generation_request_id);
CREATE INDEX idx_fp007ima_kind ON fp007_image_media_assets(asset_kind);
CREATE INDEX idx_fp007ga_status ON fp007_generated_assets(status);
CREATE INDEX idx_fp007gar_generated_asset ON fp007_generated_asset_reviews(generated_asset_id);
CREATE INDEX idx_rr_current_state ON review_records(current_state);
CREATE INDEX idx_rr_target_type_id ON review_records(review_target_type, review_target_id);
CREATE INDEX idx_rr_reviewer ON review_records(current_reviewer_actor_reference);
CREATE INDEX idx_rae_review_record ON review_audit_events(review_record_id);
CREATE INDEX idx_rae_occurred_at ON review_audit_events(occurred_at);
CREATE INDEX idx_rae_record_time ON review_audit_events(review_record_id, occurred_at);
CREATE INDEX idx_au_username ON admin_users(username);
CREATE INDEX idx_au_role ON admin_users(role);
