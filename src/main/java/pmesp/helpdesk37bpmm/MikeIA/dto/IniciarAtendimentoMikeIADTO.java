package pmesp.helpdesk37bpmm.MikeIA.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;

// Dados enviados pelo usuário para começar uma conversa com o Mike IA
@Data
@NoArgsConstructor
@AllArgsConstructor
public class IniciarAtendimentoMikeIADTO {

    @NotBlank(message = "Descreva o problema para o Mike IA poder ajudar.")
    private String descricaoProblema;

    // Opcional: se o usuário já sabe a categoria, o Mike acerta a sugestão mais rápido
    private ChamadoCategoria categoria;
}
