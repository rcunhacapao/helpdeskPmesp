package pmesp.helpdesk37bpmm.Chamado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;

// Somente dados necessários para cadastrar um novo chamado
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChamadoDTO {

    @NotBlank(message = "Informe o seu RE.")
    private String re;

    @NotBlank(message = "Informe o problema.")
    private String descricao;

    @NotNull(message = "Selecione uma categoria para o chamado.")
    private ChamadoCategoria categoria;

    @NotBlank(message = "Informe o local de atendimento.")
    private String localAtendimento;

    @NotNull(message = "Selecione a prioridade do chamado.")
    private ChamadoPrioridade prioridade;
}
