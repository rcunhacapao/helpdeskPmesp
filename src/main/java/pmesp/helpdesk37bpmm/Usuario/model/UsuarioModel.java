package pmesp.helpdesk37bpmm.Usuario.model;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;

@Entity
@Table(name = "tb_usuario")
@NoArgsConstructor
@Getter
@Setter
public class UsuarioModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "Posto_Graduacao", nullable = false)
    @Enumerated(EnumType.STRING)
    private UsuarioPostoGraduacao postoGraduacao;

    @Column(name = "QRA", nullable = false)
    private String nome;

    @Column(name = "RE", unique = true, nullable = false)
    private String re;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "troca_senha_obrigatoria", nullable = false)
    private boolean trocaSenhaObrigatoria;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @JsonValue
    public String getIdentificacaoCompleta() {
        return postoGraduacao.getDescricao() + " " + nome;
    }
}
