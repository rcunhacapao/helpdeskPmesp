package db.migration;

import org.flywaydb.core.api.migration.Context;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AtualizarFluxoDeSenhasTest {

    @Test
    void devePreservarContasComSenhaEPrepararContasDoFluxoAntigo() throws Exception {
        try (Connection conexao = DriverManager.getConnection("jdbc:h2:mem:migracao_senhas;DB_CLOSE_DELAY=-1")) {
            try (Statement comando = conexao.createStatement()) {
                comando.execute("""
                        CREATE TABLE tb_usuario (
                            id BIGINT PRIMARY KEY,
                            re VARCHAR(20) NOT NULL,
                            email VARCHAR(255) NOT NULL,
                            senha_hash VARCHAR(255)
                        )
                        """);
                comando.executeUpdate("""
                        INSERT INTO tb_usuario (id, re, email, senha_hash)
                        VALUES (1, '111111', 'novo@policiamilitar.sp.gov.br', NULL)
                        """);
                comando.executeUpdate("""
                        INSERT INTO tb_usuario (id, re, email, senha_hash)
                        VALUES (2, '222222', 'antigo@policiamilitar.sp.gov.br', 'hash-existente')
                        """);
            }

            Context contexto = mock(Context.class);
            when(contexto.getConnection()).thenReturn(conexao);
            new V8__atualizar_fluxo_de_senhas().migrate(contexto);

            try (Statement consulta = conexao.createStatement()) {
                ResultSet novoFluxo = consulta.executeQuery(
                        "SELECT senha_hash, troca_senha_obrigatoria FROM tb_usuario WHERE id = 1");
                novoFluxo.next();
                assertTrue(new BCryptPasswordEncoder().matches("111111", novoFluxo.getString("senha_hash")));
                assertTrue(novoFluxo.getBoolean("troca_senha_obrigatoria"));

                ResultSet contaExistente = consulta.executeQuery(
                        "SELECT senha_hash, troca_senha_obrigatoria FROM tb_usuario WHERE id = 2");
                contaExistente.next();
                assertTrue("hash-existente".equals(contaExistente.getString("senha_hash")));
                assertFalse(contaExistente.getBoolean("troca_senha_obrigatoria"));

                try (Statement insercaoSemEmail = conexao.createStatement()) {
                    insercaoSemEmail.executeUpdate("INSERT INTO tb_usuario (id, re, email, senha_hash) "
                            + "VALUES (3, '333333', NULL, 'hash')");
                }
            }
        }
    }
}
