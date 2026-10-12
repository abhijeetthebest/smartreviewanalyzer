-- Optional schema initialization (pgvector extension is usually handled by Spring AI)
-- Make sure the database has the vector extension enabled.

CREATE EXTENSION IF NOT EXISTS vector;

-- The reviews table is managed by JPA (ddl-auto=update)
-- Spring AI will create the vector store tables automatically when initialize-schema=true
