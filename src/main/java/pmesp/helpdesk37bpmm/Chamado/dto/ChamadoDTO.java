package pmesp.helpdesk37bpmm.Chamado.dto;

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

    private String re;
    private String descricao;
    private ChamadoCategoria categoria;
    private String localAtendimento;
    private ChamadoPrioridade prioridade;
}
