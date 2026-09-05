-- O banco H2 existente foi criado antes do fluxo integrado com o Mike e ainda
-- limitava a coluna aos quatro status antigos. A entidade Java já conhece os
-- novos valores; esta migração mantém o esquema persistente sincronizado.
ALTER TABLE IF EXISTS tb_chamados
    ALTER COLUMN status ENUM(
        'EM_DIAGNOSTICO',
        'ABERTO',
        'FECHADO',
        'EM_ATENDIMENTO',
        'CANCELADO',
        'ABANDONADO'
    ) NOT NULL;

-- O resultado permanece nulo enquanto o usuário ainda está seguindo as
-- orientações, por isso esta coluna não recebe NOT NULL.
ALTER TABLE IF EXISTS tb_atendimento_mike_ia
    ALTER COLUMN resultado ENUM(
        'RESOLVIDO',
        'ENCAMINHADO_PARA_CHAMADO',
        'ABANDONADO'
    );
