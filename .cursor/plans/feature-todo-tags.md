[현재 테이블 구조]

-- V1-V4

CREATE TABLE todos (
    seq BIGSERIAL PRIMARY KEY,
    id VARCHAR(36) NOT NULL UNIQUE,

    title VARCHAR(255) NOT NULL,
    description TEXT,

    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    is_deleted   BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================================================
-- Tags table (1:N relationship with todos)
-- =========================================================

CREATE TABLE tags (
    seq BIGSERIAL PRIMARY KEY,
    id VARCHAR(26) NOT NULL UNIQUE,

    todo_seq BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_tags_todo_seq
        FOREIGN KEY (todo_seq)
        REFERENCES todos(seq)
        ON DELETE CASCADE
);

-- =========================================================
-- Indexes for todos
-- =========================================================

CREATE INDEX idx_todos_seq ON todos(seq);
CREATE INDEX idx_todos_is_completed ON todos(is_completed);
CREATE INDEX idx_todos_is_deleted ON todos(is_deleted);

CREATE INDEX idx_todos_active_seq
    ON todos(is_deleted, seq);

-- =========================================================
-- Indexes for tags
-- =========================================================

CREATE INDEX idx_tags_seq ON tags(seq);
CREATE INDEX idx_tags_todo_seq ON tags(todo_seq);
CREATE INDEX idx_tags_name ON tags(name);



---

1. tags가 추가되었고, 1:N 구조가 될 것임.

2. N + 1 문제 안 일어나도록 /todo 기존 api에 tag도 같이 넘어오도록 

3. /tags 로 tag의 distinct values -> [중요] 이건 paging api야.

4. /tag/{tag}로 특정 태그가 들어있는 todo 리턴 