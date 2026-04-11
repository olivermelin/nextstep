-- NextStep — Socialt aktivitetsflöde (Fas 1)
-- feed_item, feed_reaction, feed_comment (Fas 2-tabell skapas nu för att undvika V3-migrering)
-- Delningsinställningar läggs till user_settings

-- ============================================================
-- Flödesinlägg (auto-genererade från aktiviteter)
-- ============================================================

CREATE TABLE feed_item (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,
    type         VARCHAR(50)  NOT NULL,
    reference_id BIGINT,
    message      VARCHAR(500) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    metadata     VARCHAR(1000),
    created_at   TIMESTAMP    NOT NULL,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_feed_item_user_id    ON feed_item(user_id);
CREATE INDEX idx_feed_item_created_at ON feed_item(created_at);
CREATE INDEX idx_feed_item_user_del   ON feed_item(user_id, deleted);

-- ============================================================
-- Heja-reaktioner (en per användare per inlägg)
-- ============================================================

CREATE TABLE feed_reaction (
    id           BIGSERIAL    PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,
    feed_item_id BIGINT       NOT NULL REFERENCES feed_item(id) ON DELETE CASCADE,
    display_name VARCHAR(255) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    CONSTRAINT uk_feed_reaction_user_item UNIQUE (user_id, feed_item_id)
);

CREATE INDEX idx_feed_reaction_item ON feed_reaction(feed_item_id);

-- ============================================================
-- Kommentarer (Fas 2 — tabell skapas nu, används inte ännu)
-- ============================================================

CREATE TABLE feed_comment (
    id           BIGSERIAL    PRIMARY KEY,
    feed_item_id BIGINT       NOT NULL REFERENCES feed_item(id) ON DELETE CASCADE,
    user_id      VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    content      VARCHAR(200) NOT NULL,
    created_at   TIMESTAMP    NOT NULL,
    deleted      BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_feed_comment_item ON feed_comment(feed_item_id);
CREATE INDEX idx_feed_comment_user ON feed_comment(user_id);

-- ============================================================
-- Delningsinställningar i user_settings
-- ============================================================

ALTER TABLE user_settings
    ADD COLUMN feed_sharing_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- ElementCollection för valda feed-typer att dela
CREATE TABLE user_settings_feed_shared_types (
    user_settings_id BIGINT       NOT NULL REFERENCES user_settings(id) ON DELETE CASCADE,
    feed_shared_types VARCHAR(50)  NOT NULL
);
