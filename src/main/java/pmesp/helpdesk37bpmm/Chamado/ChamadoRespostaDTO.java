package pmesp.helpdesk37bpmm.Chamado;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// Dados de um chamado que podem ser mostrados como resposta da API
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoRespostaDTO {

    private Long id;
    private String solicitante;
    private String descricao;
    private String localAtendimento;
    private String motivoCancelamento;
    private ChamadoPrioridade prioridade;
    private ChamadoStatus status;

    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    private LocalDateTime dataAbertura;

    @JsonFormat(pattern = "dd/MM/yyyy 'às' HH:mm")
    private LocalDateTime dataFinalizacao;
}
