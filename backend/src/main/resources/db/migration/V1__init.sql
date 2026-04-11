-- NextStep — initial schema
-- Körs automatiskt av Flyway på nya databaser.
-- Befintliga databaser (dev) baslinjesätts via baseline-on-migrate=true.

-- ============================================================
-- Grundentiteter
-- ============================================================

CREATE TABLE app_user (
    id          BIGSERIAL PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMP    NOT NULL,
    last_updated TIMESTAMP   NOT NULL
);

CREATE TABLE achievement (
    id               BIGSERIAL PRIMARY KEY,
    achievement_id   INTEGER      NOT NULL UNIQUE,
    title            VARCHAR(255) NOT NULL,
    description      VARCHAR(500) NOT NULL,
    required_points  INTEGER      NOT NULL,
    required_level   INTEGER      NOT NULL,
    created_at       TIMESTAMP    NOT NULL
);

CREATE TABLE challenge (
    id               BIGSERIAL PRIMARY KEY,
    title            VARCHAR(255)  NOT NULL,
    description      VARCHAR(255)  NOT NULL,
    duration_minutes INTEGER       NOT NULL,
    difficulty       VARCHAR(50)   NOT NULL,
    category         VARCHAR(100)  NOT NULL,
    youtube_url      VARCHAR(255),
    instructions     VARCHAR(2000),
    created_at       TIMESTAMP     NOT NULL
);

CREATE TABLE password_reset_token (
    id         BIGSERIAL PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    BIGINT       NOT NULL REFERENCES app_user(id),
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL
);

-- ============================================================
-- Användardata (BaseUserData-arvingar)
-- ============================================================

CREATE TABLE user_settings (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 VARCHAR(255) NOT NULL,
    last_updated            TIMESTAMP    NOT NULL,
    name                    VARCHAR(255),
    email                   VARCHAR(255),
    phone                   VARCHAR(255),
    language                VARCHAR(50),
    notifications_enabled   BOOLEAN,
    ai_notifications_enabled BOOLEAN,
    dark_mode_enabled       BOOLEAN,
    onboarding_completed    BOOLEAN      DEFAULT FALSE,
    onboarding_track        VARCHAR(50),
    subscription_tier       VARCHAR(50),
    coach_personality       VARCHAR(50),
    other_goal              VARCHAR(255),
    recovery_stage          VARCHAR(100),
    -- BackgroundInfo (@Embedded)
    age                     INTEGER,
    gender                  VARCHAR(50),
    current_situation       VARCHAR(100),
    ai_analysis_consent     BOOLEAN      DEFAULT FALSE,
    ai_analysis_consent_at  TIMESTAMP
);
CREATE INDEX idx_user_settings_user_id ON user_settings(user_id);

-- ElementCollection: UserSettings.userGoals
CREATE TABLE user_settings_user_goals (
    user_settings_id BIGINT      NOT NULL REFERENCES user_settings(id),
    user_goals       VARCHAR(100)
);

-- ElementCollection: BackgroundInfo.substanceHistory (inbäddad i UserSettings)
CREATE TABLE user_settings_substance_history (
    user_settings_id BIGINT      NOT NULL REFERENCES user_settings(id),
    substance_history VARCHAR(100)
);

CREATE TABLE user_streak (
    id                 BIGSERIAL PRIMARY KEY,
    user_id            VARCHAR(255) NOT NULL,
    last_updated       TIMESTAMP    NOT NULL,
    current_streak     INTEGER      NOT NULL DEFAULT 0,
    longest_streak     INTEGER      NOT NULL DEFAULT 0,
    last_activity_date DATE,
    streak_start_date  DATE
);
CREATE INDEX idx_user_streak_user_id ON user_streak(user_id);

CREATE TABLE user_progress (
    id           BIGSERIAL PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,
    last_updated TIMESTAMP    NOT NULL,
    total_points INTEGER      NOT NULL DEFAULT 0,
    level        INTEGER      NOT NULL DEFAULT 1
);
CREATE INDEX idx_user_progress_user_id ON user_progress(user_id);

CREATE TABLE daily_check_in (
    id            BIGSERIAL PRIMARY KEY,
    user_id       VARCHAR(255) NOT NULL,
    last_updated  TIMESTAMP    NOT NULL,
    check_in_date DATE         NOT NULL,
    mood_score    INTEGER      NOT NULL,
    note          VARCHAR(500),
    CONSTRAINT uk_daily_checkin_user_date UNIQUE (user_id, check_in_date)
);
CREATE INDEX idx_daily_checkin_user_id ON daily_check_in(user_id);

-- ============================================================
-- Utmaningar och prestationer
-- ============================================================

CREATE TABLE user_challenge (
    id            BIGSERIAL PRIMARY KEY,
    user_id       VARCHAR(255) NOT NULL,
    challenge_id  BIGINT       NOT NULL REFERENCES challenge(id),
    completed     BOOLEAN      NOT NULL,
    completed_at  TIMESTAMP,
    points_earned INTEGER      NOT NULL,
    actual_minutes INTEGER     NOT NULL,
    started_at    TIMESTAMP    NOT NULL
);
CREATE INDEX idx_user_challenge_user_id ON user_challenge(user_id);
CREATE INDEX idx_user_challenge_user_completed ON user_challenge(user_id, completed);

CREATE TABLE user_achievement (
    id             BIGSERIAL PRIMARY KEY,
    user_id        VARCHAR(255) NOT NULL,
    achievement_id BIGINT       NOT NULL REFERENCES achievement(id),
    unlocked       BOOLEAN      NOT NULL,
    unlocked_at    TIMESTAMP
);

CREATE TABLE category_progress (
    id           BIGSERIAL PRIMARY KEY,
    user_id      VARCHAR(255) NOT NULL,
    category     VARCHAR(100) NOT NULL,
    points       INTEGER      NOT NULL,
    last_updated TIMESTAMP    NOT NULL
);

-- ============================================================
-- AI Coach
-- ============================================================

CREATE TABLE coach_sessions (
    id              BIGSERIAL PRIMARY KEY,
    session_id      VARCHAR(255) NOT NULL UNIQUE,
    user_id         VARCHAR(255) NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL,
    last_message_at TIMESTAMP    NOT NULL
);

CREATE TABLE coach_messages (
    id              BIGSERIAL PRIMARY KEY,
    session_id      BIGINT       NOT NULL REFERENCES coach_sessions(id),
    role            VARCHAR(50)  NOT NULL,
    content         TEXT         NOT NULL,
    crisis_detected BOOLEAN      NOT NULL,
    crisis_level    VARCHAR(50),
    timestamp       TIMESTAMP    NOT NULL
);

CREATE TABLE daily_message_quota (
    id            BIGSERIAL PRIMARY KEY,
    user_id       VARCHAR(255) NOT NULL,
    quota_date    DATE         NOT NULL,
    message_count INTEGER      NOT NULL DEFAULT 0,
    CONSTRAINT uq_quota_user_date UNIQUE (user_id, quota_date)
);
CREATE INDEX idx_quota_user_date ON daily_message_quota(user_id, quota_date);

-- ============================================================
-- Belöningar
-- ============================================================

CREATE TABLE collectible (
    id               BIGSERIAL PRIMARY KEY,
    user_id          VARCHAR(255) NOT NULL,
    collectible_key  VARCHAR(100) NOT NULL,
    collectible_type VARCHAR(50)  NOT NULL,
    unlocked_at      TIMESTAMP    NOT NULL,
    CONSTRAINT uk_collectible_user_key UNIQUE (user_id, collectible_key)
);
CREATE INDEX idx_collectible_user_id ON collectible(user_id);

CREATE TABLE daily_reward (
    id               BIGSERIAL PRIMARY KEY,
    user_id          VARCHAR(255) NOT NULL,
    reward_date      DATE         NOT NULL,
    reward_type      VARCHAR(50)  NOT NULL,
    reward_value     VARCHAR(500),
    claimed          BOOLEAN      NOT NULL DEFAULT FALSE,
    consecutive_days INTEGER      NOT NULL,
    rare             BOOLEAN      NOT NULL,
    created_at       TIMESTAMP    NOT NULL,
    CONSTRAINT uk_daily_reward_user_date UNIQUE (user_id, reward_date)
);
CREATE INDEX idx_daily_reward_user_id ON daily_reward(user_id);

-- ============================================================
-- Socialt
-- ============================================================

CREATE TABLE friendships (
    id             BIGSERIAL PRIMARY KEY,
    requester_id   VARCHAR(255) NOT NULL,
    addressee_id   VARCHAR(255) NOT NULL,
    requester_name VARCHAR(255) NOT NULL,
    status         VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP,
    CONSTRAINT uk_friendship_pair UNIQUE (requester_id, addressee_id)
);
CREATE INDEX idx_friendship_requester ON friendships(requester_id);
CREATE INDEX idx_friendship_addressee ON friendships(addressee_id);

CREATE TABLE duels (
    id                       BIGSERIAL PRIMARY KEY,
    challenger_id            VARCHAR(255) NOT NULL,
    challenged_id            VARCHAR(255) NOT NULL,
    challenger_name          VARCHAR(255) NOT NULL,
    challenged_name          VARCHAR(255) NOT NULL,
    challenge_id             BIGINT       NOT NULL,
    challenge_name           VARCHAR(255) NOT NULL,
    status                   VARCHAR(50)  NOT NULL DEFAULT 'PENDING',
    winner_id                VARCHAR(255),
    challenger_completed_at  TIMESTAMP,
    challenged_completed_at  TIMESTAMP,
    created_at               TIMESTAMP    NOT NULL,
    expires_at               TIMESTAMP,
    completed_at             TIMESTAMP
);
CREATE INDEX idx_duel_challenger ON duels(challenger_id);
CREATE INDEX idx_duel_challenged ON duels(challenged_id);
