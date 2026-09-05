package pmesp.helpdesk37bpmm.MikeIA.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;

// Dados complementares obrigatórios somente quando o atendimento precisa chegar à fila técnica.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncaminharChamadoDoMikeDTO {

    @NotNull(message = "Selecione a categoria para encaminhar o chamado.")
    private ChamadoCategoria categoria;

    @NotBlank(message = "Informe o local ou setor para encaminhar o chamado.")
    private String localAtendimento;

    @NotNull(message = "Selecione a prioridade para encaminhar o chamado.")
    private ChamadoPrioridade prioridade;
}
