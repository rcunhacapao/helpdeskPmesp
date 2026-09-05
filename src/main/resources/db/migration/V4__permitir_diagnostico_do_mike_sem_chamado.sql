-- O diagnóstico do Mike é temporário. O chamado só é criado após a confirmação
-- de resolução ou o envio do encaminhamento para a equipe técnica.
ALTER TABLE IF EXISTS tb_atendimento_mike_ia
    ALTER COLUMN chamado_gerado_id DROP NOT NULL;
