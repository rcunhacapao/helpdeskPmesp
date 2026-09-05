package pmesp.helpdesk37bpmm.MikeIA.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.MikeIA.enums.AtendimentoMikeIAResultado;

import java.time.LocalDateTime;

// Dados devolvidos ao frontend em cada etapa da conversa com o Mike IA
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoMikeIARespostaDTO {

    private Long atendimentoId;
    private Long chamadoId;
    private String descricaoProblema;
    private ChamadoCategoria categoria;
    private String sugestoes;
    private boolean possuiOrientacaoTestavel;

    // Fica nulo enquanto a conversa não foi concluída (usuário ainda não respondeu
    // se as sugestões resolveram)
    private AtendimentoMikeIAResultado resultado;

    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    private LocalDateTime dataInicio;

    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    private LocalDateTime dataConclusao;
}
