package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Converte os registros criados pelo fluxo antigo, no qual o diagnóstico do Mike
// já abria um chamado temporário. O fluxo atual só cria o chamado no envio final.
public class V7__remover_chamados_temporarios_do_mike extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conexao = context.getConnection();
        if (!tabelaExiste(conexao, "tb_chamados")
                || !tabelaExiste(conexao, "tb_atendimento_mike_ia")) {
            return;
        }

        try (Statement comando = conexao.createStatement()) {
            // Um diagnóstico já encaminhado representa um chamado real e deve ser preservado.
            comando.executeUpdate("""
                    UPDATE tb_chamados
                       SET status = 'ABERTO'
                     WHERE status IN ('EM_DIAGNOSTICO', 'ABANDONADO')
                       AND id IN (
                           SELECT chamado_gerado_id
                             FROM tb_atendimento_mike_ia
                            WHERE resultado = 'ENCAMINHADO_PARA_CHAMADO'
                       )
                    """);

            // Conversas antigas interrompidas terminam como abandonadas, sem gerar chamado.
            comando.executeUpdate("""
                    UPDATE tb_atendimento_mike_ia
                       SET resultado = 'ABANDONADO',
                           data_conclusao = COALESCE(data_conclusao, CURRENT_TIMESTAMP)
                     WHERE resultado IS NULL
                       AND chamado_gerado_id IN (
                           SELECT id
                             FROM tb_chamados
                            WHERE status IN ('EM_DIAGNOSTICO', 'ABANDONADO')
                       )
                    """);

            comando.executeUpdate("""
                    UPDATE tb_atendimento_mike_ia
                       SET chamado_gerado_id = NULL
                     WHERE resultado IN ('RESOLVIDO', 'ABANDONADO')
                       AND chamado_gerado_id IN (
                           SELECT id
                             FROM tb_chamados
                            WHERE status IN ('EM_DIAGNOSTICO', 'ABANDONADO')
                       )
                    """);

            // Exclui apenas os chamados temporários que deixaram de possuir vínculo.
            comando.executeUpdate("""
                    DELETE FROM tb_chamados
                     WHERE status IN ('EM_DIAGNOSTICO', 'ABANDONADO')
                       AND id NOT IN (
                           SELECT chamado_gerado_id
                             FROM tb_atendimento_mike_ia
                            WHERE chamado_gerado_id IS NOT NULL
                       )
                    """);

            // Proteção para algum registro legado inconsistente que ainda possua vínculo.
            comando.executeUpdate("""
                    UPDATE tb_chamados
                       SET status = 'ABERTO'
                     WHERE status IN ('EM_DIAGNOSTICO', 'ABANDONADO')
                    """);

            // Sintaxe "ALTER COLUMN x ENUM(...)" é específica do H2 e não existe no
            // PostgreSQL; a coluna já é tratada como VARCHAR pelo Hibernate
            // (@Enumerated(STRING)), então só precisamos manter o tipo e o NOT NULL.
            comando.execute("ALTER TABLE tb_chamados ALTER COLUMN status SET DATA TYPE VARCHAR(255)");
            comando.execute("ALTER TABLE tb_chamados ALTER COLUMN status SET NOT NULL");
        }
    }

    // information_schema é padrão SQL e funciona tanto em H2 quanto em PostgreSQL.
    // A comparação é case-insensitive porque H2 guarda identificadores não citados
    // em maiúsculas e o PostgreSQL guarda em minúsculas.
    private boolean tabelaExiste(Connection conexao, String nomeDaTabela) throws SQLException {
        try (PreparedStatement consulta = conexao.prepareStatement(
                "SELECT 1 FROM information_schema.tables WHERE lower(table_name) = lower(?)")) {
            consulta.setString(1, nomeDaTabela);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next();
            }
        }
    }
}
