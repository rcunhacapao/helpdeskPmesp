package pmesp.helpdesk37bpmm.Chamado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 6, message = "O RE ultrapassou o tamanho permitido.")
    private String re;

    @NotBlank(message = "Informe o problema.")
    @Size(max = 5000, message = "A descrição do problema ultrapassou o tamanho permitido.")
    private String descricao;

    @NotNull(message = "Selecione uma categoria para o chamado.")
    private ChamadoCategoria categoria;

    @NotBlank(message = "Informe o local de atendimento.")
    @Size(max = 200, message = "O local de atendimento ultrapassou o tamanho permitido.")
    private String localAtendimento;

    @NotNull(message = "Selecione a prioridade do chamado.")
    private ChamadoPrioridade prioridade;
}
