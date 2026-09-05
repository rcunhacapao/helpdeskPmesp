-- Chamados criados antes da trava otimista não possuem a coluna de versão.
-- O valor zero permite preservar esses registros e habilita o controle de concorrência.
ALTER TABLE IF EXISTS tb_chamados
    ADD COLUMN IF NOT EXISTS versao BIGINT DEFAULT 0;
