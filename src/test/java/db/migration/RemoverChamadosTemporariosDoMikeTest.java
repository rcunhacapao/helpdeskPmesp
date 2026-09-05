package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RemoverChamadosTemporariosDoMikeTest {

    @Test
    void deveRemoverSomenteChamadoTemporarioEPreservarChamadoEncaminhado() throws Exception {
        try (Connection conexao = DriverManager.getConnection("jdbc:h2:mem:migracao_mike;DB_CLOSE_DELAY=-1")) {
            prepararBancoAntigo(conexao);

            Context contexto = mock(Context.class);
            when(contexto.getConnection()).thenReturn(conexao);
            new V7__remover_chamados_temporarios_do_mike().migrate(contexto);

            try (Statement consulta = conexao.createStatement()) {
                ResultSet atendimentoAbandonado = consulta.executeQuery("""
                        SELECT resultado, chamado_gerado_id
                          FROM tb_atendimento_mike_ia
                         WHERE id = 1
                        """);
                atendimentoAbandonado.next();
                assertEquals("ABANDONADO", atendimentoAbandonado.getString("resultado"));
                assertNull(atendimentoAbandonado.getObject("chamado_gerado_id"));

                ResultSet chamadoTemporario = consulta.executeQuery("SELECT id FROM tb_chamados WHERE id = 10");
                assertFalse(chamadoTemporario.next());

                ResultSet chamadoEncaminhado = consulta.executeQuery("SELECT status FROM tb_chamados WHERE id = 20");
                chamadoEncaminhado.next();
                assertEquals("ABERTO", chamadoEncaminhado.getString("status"));
            }
        }
    }

    private void prepararBancoAntigo(Connection conexao) throws Exception {
        try (Statement comando = conexao.createStatement()) {
            comando.execute("""
                    CREATE TABLE tb_chamados (
                        id BIGINT PRIMARY KEY,
                        status ENUM('EM_DIAGNOSTICO', 'ABERTO', 'FECHADO', 'EM_ATENDIMENTO', 'CANCELADO', 'ABANDONADO') NOT NULL
                    )
                    """);
            comando.execute("""
                    CREATE TABLE tb_atendimento_mike_ia (
                        id BIGINT PRIMARY KEY,
                        resultado ENUM('RESOLVIDO', 'ENCAMINHADO_PARA_CHAMADO', 'ABANDONADO'),
                        chamado_gerado_id BIGINT,
                        data_conclusao TIMESTAMP,
                        CONSTRAINT fk_atendimento_chamado
                            FOREIGN KEY (chamado_gerado_id) REFERENCES tb_chamados(id)
                    )
                    """);

            comando.executeUpdate("INSERT INTO tb_chamados (id, status) VALUES (10, 'EM_DIAGNOSTICO')");
            comando.executeUpdate("INSERT INTO tb_chamados (id, status) VALUES (20, 'EM_DIAGNOSTICO')");
            comando.executeUpdate("""
                    INSERT INTO tb_atendimento_mike_ia (id, resultado, chamado_gerado_id)
                    VALUES (1, NULL, 10)
                    """);
            comando.executeUpdate("""
                    INSERT INTO tb_atendimento_mike_ia (id, resultado, chamado_gerado_id)
                    VALUES (2, 'ENCAMINHADO_PARA_CHAMADO', 20)
                    """);
        }
    }
}
