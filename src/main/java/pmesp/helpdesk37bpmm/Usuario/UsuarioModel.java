package pmesp.helpdesk37bpmm.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.ChamadoModel;

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

    @Column(name = "Posto_Graduacao", nullable = false)
    @Enumerated(EnumType.STRING)
    private UsuarioPostoGraduacao postoGraduacao;

    @Column(name = "QRA", nullable = false)
    private String nome;

    @Column(name = "RE", unique = true, nullable = false)
    private String re;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

}
