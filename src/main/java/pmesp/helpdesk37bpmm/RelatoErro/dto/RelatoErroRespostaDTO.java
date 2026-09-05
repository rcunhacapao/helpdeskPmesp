package pmesp.helpdesk37bpmm.RelatoErro.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.RelatoErro.enums.TipoDeErro;

import java.time.LocalDateTime;

// Dados de um relato de erro que podem ser mostrados na área técnica
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelatoErroRespostaDTO {

    private Long id;
    private TipoDeErro tipoErro;
    private String observacao;
    private String identificacaoDeQuemRelatou;
    private LocalDateTime dataRelato;
}
