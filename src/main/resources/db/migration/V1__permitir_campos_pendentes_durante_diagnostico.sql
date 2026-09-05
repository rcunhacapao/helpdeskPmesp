-- Durante o diagnóstico do Mike, o chamado já existe, mas categoria e local
-- ainda podem não ter sido informados. Eles continuam obrigatórios antes do
-- encaminhamento para a equipe técnica, conforme a regra do serviço.
--
-- IF EXISTS mantém a inicialização compatível com bancos novos: nesse caso,
-- o Hibernate cria a tabela depois com a definição atual da entidade.
ALTER TABLE IF EXISTS tb_chamados
    ALTER COLUMN categoria DROP NOT NULL;

ALTER TABLE IF EXISTS tb_chamados
    ALTER COLUMN local_atendimento DROP NOT NULL;
