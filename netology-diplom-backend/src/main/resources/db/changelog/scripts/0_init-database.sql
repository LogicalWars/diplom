CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    login VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE files (
    id BIGSERIAL PRIMARY KEY,
    filename TEXT NOT NULL,
    hash TEXT,
    content_type TEXT,
    size BIGINT,
    data BYTEA NOT NULL,
    uploaded_at TIMESTAMP DEFAULT now(),
    user_id BIGINT REFERENCES users(id)
);

