-- Chamados resolvidos antes do encaminhamento técnico não receberam prioridade.
ALTER TABLE IF EXISTS tb_chamados
    ALTER COLUMN prioridade DROP NOT NULL;
