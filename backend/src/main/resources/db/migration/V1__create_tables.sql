CREATE TABLE app_users
(
  id            UUID PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  display_name  VARCHAR(40)  NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE TABLE posts
(
  id         UUID PRIMARY KEY,
  author_id  UUID         NOT NULL REFERENCES app_users (id),
  content    VARCHAR(500) NOT NULL,
  created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_posts_created_at ON posts (created_at DESC);
