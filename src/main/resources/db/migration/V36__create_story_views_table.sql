CREATE TABLE story_views
(
    id        UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    story_id  UUID      NOT NULL,
    user_id   UUID      NOT NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_story_views_story FOREIGN KEY (story_id)
        REFERENCES stories (id) ON DELETE CASCADE,
    CONSTRAINT fk_story_views_user FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT uq_story_views_story_user UNIQUE (story_id, user_id)
);

CREATE INDEX idx_story_views_story_id ON story_views (story_id);
CREATE INDEX idx_story_views_user_id ON story_views (user_id);