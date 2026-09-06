package pmesp.helpdesk37bpmm.MikeIA.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 5000, message = "A descrição do problema ultrapassou o tamanho permitido.")
    private String descricaoProblema;

    @NotNull(message = "Escolha uma categoria para iniciar o atendimento.")
    private ChamadoCategoria categoria;
}
