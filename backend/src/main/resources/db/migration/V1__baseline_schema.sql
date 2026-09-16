CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(320) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(80) NOT NULL,
    role VARCHAR(30) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_users_email UNIQUE (email)
);

CREATE TABLE palettes (
    id UUID PRIMARY KEY,
    created_by UUID NOT NULL REFERENCES users(id),
    name VARCHAR(80) NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    like_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_palettes_like_count CHECK (like_count >= 0)
);

CREATE INDEX idx_palettes_newest ON palettes (status, published_at DESC, id);
CREATE INDEX idx_palettes_popular ON palettes (status, like_count DESC, published_at DESC, id);

CREATE TABLE palette_colors (
    palette_id UUID NOT NULL REFERENCES palettes(id) ON DELETE CASCADE,
    position SMALLINT NOT NULL,
    hex_value CHAR(7) NOT NULL,
    PRIMARY KEY (palette_id, position),
    CONSTRAINT ck_palette_colors_position CHECK (position BETWEEN 0 AND 3),
    CONSTRAINT ck_palette_colors_hex CHECK (hex_value LIKE '#______')
);

CREATE TABLE tags (
    id UUID PRIMARY KEY,
    slug VARCHAR(60) NOT NULL,
    display_name VARCHAR(60) NOT NULL,
    CONSTRAINT uq_tags_slug UNIQUE (slug)
);

CREATE TABLE palette_tags (
    palette_id UUID NOT NULL REFERENCES palettes(id) ON DELETE CASCADE,
    tag_id UUID NOT NULL REFERENCES tags(id) ON DELETE CASCADE,
    PRIMARY KEY (palette_id, tag_id)
);

CREATE TABLE favorites (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    palette_id UUID NOT NULL REFERENCES palettes(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (user_id, palette_id)
);

CREATE INDEX idx_favorites_user_created ON favorites (user_id, created_at DESC, palette_id);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_refresh_tokens_hash UNIQUE (token_hash)
);
