-- O frontend precisa saber se recebeu uma orientação prática ou apenas um encaminhamento.
-- O dado é persistido para manter o comportamento correto após recarregar a página.
ALTER TABLE IF EXISTS tb_atendimento_mike_ia
    ADD COLUMN IF NOT EXISTS possui_orientacao_testavel BOOLEAN NOT NULL DEFAULT FALSE;
