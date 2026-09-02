package pmesp.helpdesk37bpmm.Tecnico.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

@Entity
@Table(name = "tb_tecnico")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class TecnicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Usuário que faz parte da equipe de Telemática
    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private UsuarioModel usuario;

    // Informar se o técnico está disponível para receber chamados agora
    @Column(name = "disponivel", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean disponivel = false;
}
