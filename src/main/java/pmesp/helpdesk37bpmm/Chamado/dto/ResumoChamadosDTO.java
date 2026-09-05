package pmesp.helpdesk37bpmm.Chamado.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Contagem de chamados para os cartões da Central Técnica. Chamados cancelados nunca
// entram nessa contagem, em nenhum dos três períodos.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumoChamadosDTO {

    private long chamadosHoje;
    private long chamadosSemana;
    private long chamadosMes;
}
