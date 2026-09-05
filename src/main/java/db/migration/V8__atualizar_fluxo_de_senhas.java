package db.migration;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// Adapta somente bancos já existentes. Em bancos novos, o Hibernate cria as colunas
// diretamente a partir da entidade e os cadastros já nascem no fluxo RE/RE.
public class V8__atualizar_fluxo_de_senhas extends BaseJavaMigration {

    @Override
    public void migrate(Context context) throws Exception {
        Connection conexao = context.getConnection();
        if (!tabelaExiste(conexao, "tb_usuario")) {
            return;
        }

        try (Statement comando = conexao.createStatement()) {
            comando.execute("ALTER TABLE tb_usuario ALTER COLUMN email DROP NOT NULL");
            comando.execute("ALTER TABLE tb_usuario ADD COLUMN IF NOT EXISTS "
                    + "troca_senha_obrigatoria BOOLEAN NOT NULL DEFAULT FALSE");
        }

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        try (PreparedStatement consulta = conexao.prepareStatement(
                "SELECT id, re FROM tb_usuario WHERE senha_hash IS NULL");
             ResultSet usuariosSemSenha = consulta.executeQuery();
             PreparedStatement atualizacao = conexao.prepareStatement(
                     "UPDATE tb_usuario SET senha_hash = ?, troca_senha_obrigatoria = TRUE WHERE id = ?")) {
            while (usuariosSemSenha.next()) {
                atualizacao.setString(1, encoder.encode(usuariosSemSenha.getString("re")));
                atualizacao.setLong(2, usuariosSemSenha.getLong("id"));
                atualizacao.addBatch();
            }
            atualizacao.executeBatch();
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
