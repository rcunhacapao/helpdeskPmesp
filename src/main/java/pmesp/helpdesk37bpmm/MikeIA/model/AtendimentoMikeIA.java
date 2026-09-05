package pmesp.helpdesk37bpmm.MikeIA.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.MikeIA.enums.AtendimentoMikeIAResultado;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.time.LocalDateTime;

// Guarda as orientações apresentadas pelo Mike e o resultado do diagnóstico.
// O chamado só é criado quando o usuário confirma a resolução ou envia o
// encaminhamento para a equipe técnica.
@Entity
@Table(name = "tb_atendimento_mike_ia")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AtendimentoMikeIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private UsuarioModel usuario;

    // Pode ficar nulo quando o usuário não informou e o Mike não conseguiu identificar
    @Enumerated(EnumType.STRING)
    private ChamadoCategoria categoria;

    @Column(name = "descricao_problema", nullable = false, columnDefinition = "TEXT")
    private String descricaoProblema;

    @Column(name = "sugestoes_apresentadas", columnDefinition = "TEXT")
    private String sugestoesApresentadas;

    @Column(name = "possui_orientacao_testavel", nullable = false)
    private boolean possuiOrientacaoTestavel;

    // Fica nulo enquanto a conversa ainda não terminou (usuário ainda não respondeu
    // se funcionou ou não)
    @Enumerated(EnumType.STRING)
    private AtendimentoMikeIAResultado resultado;

    // Fica nulo enquanto o usuário ainda está avaliando as orientações do Mike.
    // Depois da confirmação final, aponta para o chamado criado naquele momento.
    @OneToOne
    @JoinColumn(name = "chamado_gerado_id", nullable = true)
    private ChamadoModel chamado;

    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDateTime dataInicio;

    @Column(name = "data_conclusao")
    private LocalDateTime dataConclusao;
}
