package pmesp.helpdesk37bpmm.Tecnico.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

@Entity
@Table(name = "tb_tecnico")
@NoArgsConstructor
@Getter
@Setter
public class TecnicoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private UsuarioModel usuario;

    @Column(name = "disponivel", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean disponivel = false;
}
