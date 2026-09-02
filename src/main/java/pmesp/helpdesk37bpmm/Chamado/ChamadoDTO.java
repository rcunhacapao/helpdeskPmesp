package pmesp.helpdesk37bpmm.Chamado;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
