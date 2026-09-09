package pmesp.helpdesk37bpmm.Chamado.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoResolvidoPor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_chamados")
@NoArgsConstructor
@Getter
@Setter
public class ChamadoModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    // Evita que dois técnicos sobrescrevam o mesmo chamado ao mesmo tempo.
    @Version
    @Column(name = "versao")
    private Long versao;

    @ManyToOne
    @JoinColumn(name = "solicitante_id", nullable = false)
    private UsuarioModel solicitante;

    // Preenchido somente quando um técnico abre o chamado para outra pessoa.
    @ManyToOne
    @JoinColumn(name = "aberto_por_id")
    private UsuarioModel abertoPor;

    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private TecnicoModel tecnicoResponsavel;

    @Column(name = "Descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column
    private ChamadoCategoria categoria;

    @Column(name = "local_atendimento")
    private String localAtendimento;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "solucao", columnDefinition = "TEXT")
    private String solucao;

    @Enumerated(EnumType.STRING)
    @Column
    private ChamadoPrioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChamadoStatus status;

    // Permite separar resoluções automáticas das feitas pela equipe técnica.
    @Enumerated(EnumType.STRING)
    @Column(name = "resolvido_por")
    private ChamadoResolvidoPor resolvidoPor;

    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    // Permanece opcional para aceitar registros anteriores a esse controle.
    @Column(name = "data_ultima_interacao")
    private LocalDateTime dataUltimaInteracao;

    @Setter(AccessLevel.PRIVATE)
    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    public void finalizarAtendimento() {
        this.dataFinalizacao = LocalDateTime.now();
        this.dataUltimaInteracao = this.dataFinalizacao;
    }

    public void registrarInteracao() {
        this.dataUltimaInteracao = LocalDateTime.now();
    }
}
