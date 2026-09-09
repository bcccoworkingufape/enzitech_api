# Migrations (Flyway)

A partir desta pasta, toda alteração de schema deve ser feita via um script SQL
versionado, nunca mais pelo Hibernate (`ddl-auto` está fixo em `validate`).

## Convenção

- Nome do arquivo: `V<versão>__descricao_curta.sql` (dois underscores após a versão).
  Ex.: `V2__add_status_column_experiments.sql`.
- Versões existentes até a adoção do Flyway já estão registradas como *baseline*
  (versão 1) — a próxima migration real deve começar em `V2__...`.
- Nunca edite um script já mesclado na `master`: como o deploy é automático,
  qualquer script já aplicado em produção não pode ser alterado — crie uma nova
  migration para corrigir.
- Ao alterar uma entidade em `model/`, crie a migration SQL correspondente no
  mesmo PR. A aplicação falha ao subir (`ddl-auto=validate`) se o schema real
  não bater com o mapeamento das entidades.
