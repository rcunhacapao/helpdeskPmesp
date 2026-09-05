package pmesp.helpdesk37bpmm.Usuario.model;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;

@Entity
@Table(name = "tb_usuario")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;


    // Posto ou Graduação
    @Column(name = "Posto_Graduacao", nullable = false)
    @Enumerated(EnumType.STRING)
    private UsuarioPostoGraduacao postoGraduacao;

    // Nome de guerra
    @Column(name = "QRA", nullable = false)
    private String nome;

    // O re deve ser usado sempre SEM DIGITO.
    @Column(name = "RE", unique = true, nullable = false)
    private String re;

    // E-mail funcional opcional. Quando informado, continua sendo armazenado para o cadastro.
    @Column(name = "email", unique = true)
    private String email;

    // Guarda somente o hash da senha; o valor original nunca é persistido.
    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    // Impede o acesso ao restante do sistema até o usuário definir uma senha pessoal.
    @Column(name = "troca_senha_obrigatoria", nullable = false)
    private boolean trocaSenhaObrigatoria;

    // O usuario será considerado ativo automaticamente quando for cadastrado no BD.
    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    // Mostrar identificacao ex: 3° SGT PM Fulano
    @JsonValue
    public String getIdentificacaoCompleta() {
        return postoGraduacao.getDescricao() + " " + nome;
    }

}
