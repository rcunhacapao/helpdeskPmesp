package pmesp.helpdesk37bpmm.RelatoErro.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pmesp.helpdesk37bpmm.RelatoErro.enums.TipoDeErro;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.time.LocalDateTime;

// Feedback sobre o sistema; não representa um chamado de suporte.
@Entity
@Table(name = "tb_relato_erro")
@NoArgsConstructor
@Getter
@Setter
public class RelatoDeErro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioModel usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_erro", nullable = false)
    private TipoDeErro tipoErro;

    // Obrigatória somente quando tipoErro = OUTRO; a regra é validada no service.
    @Column(name = "observacao", columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "data_relato", nullable = false, updatable = false)
    private LocalDateTime dataRelato;
}
