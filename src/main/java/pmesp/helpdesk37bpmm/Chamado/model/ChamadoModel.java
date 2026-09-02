package pmesp.helpdesk37bpmm.Chamado.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_chamados")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChamadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private UsuarioModel solicitante;

    // Técnico que será responsável pelo atendimento
    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private TecnicoModel tecnicoResponsavel;

    @Column(name = "Descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    // Tipo do problema informado no chamado
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChamadoCategoria categoria;

    // Local onde o atendimento será realizado
    @Column(name = "local_atendimento", nullable = false)
    private String localAtendimento;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChamadoPrioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChamadoStatus status;

    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    // Blindar a data contra alterações manuais
    @Setter(AccessLevel.PRIVATE)
    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    public void finalizarAtendimento() {
        this.dataFinalizacao = LocalDateTime.now();
    }
}
