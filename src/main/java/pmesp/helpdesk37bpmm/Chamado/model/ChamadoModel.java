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
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoResolvidoPor;
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

    // Fica nulo quando o próprio solicitante abriu o chamado. É preenchido só quando um técnico
    // registra o atendimento em nome de outra pessoa (ex.: policial relatou o problema pessoalmente).
    @ManyToOne
    @JoinColumn(name = "aberto_por_id")
    private UsuarioModel abertoPor;

    // Técnico que será responsável pelo atendimento
    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private TecnicoModel tecnicoResponsavel;

    @Column(name = "Descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    // Tipo do problema informado no chamado
    @Enumerated(EnumType.STRING)
    // A categoria pode ser completada depois do diagnóstico inicial do Mike IA.
    @Column
    private ChamadoCategoria categoria;

    // Local onde o atendimento será realizado
    // O local é obrigatório antes do encaminhamento técnico, mas não na primeira descrição.
    @Column(name = "local_atendimento")
    private String localAtendimento;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    // Preenchida pelo técnico ao finalizar o atendimento. Fica em branco quando ele não
    // registrar nada — não é obrigatória.
    @Column(name = "solucao", columnDefinition = "TEXT")
    private String solucao;

    @Enumerated(EnumType.STRING)
    // A prioridade só existe quando o chamado precisa ser atendido pela equipe técnica.
    // Chamados encerrados diretamente pelo Mike IA ficam sem prioridade.
    @Column
    private ChamadoPrioridade prioridade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChamadoStatus status;

    // Preenchido somente quando o chamado foi resolvido. Mantém o status simples e
    // permite medir separadamente resoluções do Mike IA e da equipe técnica.
    @Enumerated(EnumType.STRING)
    @Column(name = "resolvido_por")
    private ChamadoResolvidoPor resolvidoPor;

    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    @Column(name = "data_abertura", nullable = false, updatable = false)
    private LocalDateTime dataAbertura;

    // Usada para recuperar diagnósticos após recarregar a página e identificar abandono.
    // Mantido opcional no banco enquanto ainda não há uma migração formal para preencher
    // registros antigos; todo novo chamado recebe este valor pelo serviço.
    @Column(name = "data_ultima_interacao")
    private LocalDateTime dataUltimaInteracao;

    // Blindar a data contra alterações manuais
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
