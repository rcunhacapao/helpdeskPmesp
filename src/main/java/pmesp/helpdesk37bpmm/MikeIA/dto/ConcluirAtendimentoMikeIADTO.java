package pmesp.helpdesk37bpmm.MikeIA.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Enviado quando o usuário confirma que o Mike resolveu o problema. O resumo
// fica no atendimento do Mike; nenhum chamado é criado nessa situação.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConcluirAtendimentoMikeIADTO {

    @NotBlank(message = "O resumo da triagem é obrigatório para concluir o atendimento.")
    @Size(max = 10000, message = "O resumo da triagem ultrapassou o tamanho permitido.")
    private String resumoAtendimento;
}
