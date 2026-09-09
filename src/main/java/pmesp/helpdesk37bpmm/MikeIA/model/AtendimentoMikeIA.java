package pmesp.helpdesk37bpmm.MikeIA.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.MikeIA.enums.AtendimentoMikeIAResultado;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.time.LocalDateTime;

// O chamado só existe quando o diagnóstico é encaminhado à equipe técnica.
@Entity
@Table(name = "tb_atendimento_mike_ia")
@NoArgsConstructor
@Getter
@Setter
public class AtendimentoMikeIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioModel usuario;

    @Enumerated(EnumType.STRING)
    private ChamadoCategoria categoria;

    @Column(name = "descricao_problema", nullable = false, columnDefinition = "TEXT")
    private String descricaoProblema;

    @Column(name = "sugestoes_apresentadas", columnDefinition = "TEXT")
    private String sugestoesApresentadas;

    @Column(name = "possui_orientacao_testavel", nullable = false)
    private boolean possuiOrientacaoTestavel;

    @Enumerated(EnumType.STRING)
    private AtendimentoMikeIAResultado resultado;

    @OneToOne
    @JoinColumn(name = "chamado_gerado_id", nullable = true)
    private ChamadoModel chamado;

    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;
}
