CREATE TABLE moderation_logs (
    id UUID PRIMARY KEY,
    palette_id UUID NOT NULL REFERENCES palettes(id) ON DELETE CASCADE,
    actor_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action VARCHAR(30) NOT NULL,
    reason VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_moderation_logs_palette ON moderation_logs (palette_id, created_at DESC);
