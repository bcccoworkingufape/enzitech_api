-- Versão da foto atual (null = sem foto). Fica em users para o front montar a URL
-- versionada sem precisar consultar a tabela de imagens.
ALTER TABLE users ADD COLUMN avatar_updated_at TIMESTAMP;

-- Bytes da foto em tabela separada: carregar um User (o que acontece em toda
-- requisição autenticada) nunca traz a imagem junto.
CREATE TABLE user_avatars (
    user_id         UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    content_type    VARCHAR(20) NOT NULL,
    content         BYTEA NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
