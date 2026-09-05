-- O banco H2 existente foi criado antes do fluxo integrado com o Mike e ainda
-- limitava a coluna aos quatro status antigos. A entidade Java já conhece os
-- novos valores; esta migração mantém o esquema persistente sincronizado.
--
-- A sintaxe "ALTER COLUMN x ENUM(...)" é específica do H2 e não existe no
-- PostgreSQL. A entidade já mapeia estes campos como @Enumerated(STRING), ou
-- seja, o Hibernate sempre trata a coluna como VARCHAR comum (igual aos
-- demais enums do sistema) — "SET DATA TYPE VARCHAR" é portável entre os
-- dois bancos e não muda nenhuma regra de negócio.
ALTER TABLE IF EXISTS tb_chamados
    ALTER COLUMN status SET DATA TYPE VARCHAR(255);
ALTER TABLE IF EXISTS tb_chamados
    ALTER COLUMN status SET NOT NULL;

-- O resultado permanece nulo enquanto o usuário ainda está seguindo as
-- orientações, por isso esta coluna não recebe NOT NULL.
ALTER TABLE IF EXISTS tb_atendimento_mike_ia
    ALTER COLUMN resultado SET DATA TYPE VARCHAR(255);
