package pmesp.helpdesk37bpmm.Tecnico.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados de técnico que podem ser mostrados na área técnica da API
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TecnicoRespostaDTO {

    private Long id;
    private String identificacao;
    private String re;
    private boolean disponivel;
}
