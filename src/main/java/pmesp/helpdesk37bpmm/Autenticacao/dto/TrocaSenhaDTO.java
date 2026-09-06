package pmesp.helpdesk37bpmm.Autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Senha digitada duas vezes durante a troca obrigatória
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrocaSenhaDTO {

    @NotBlank(message = "Informe a nova senha.")
    @Size(max = 72, message = "A nova senha ultrapassou o tamanho permitido.")
    private String novaSenha;

    @NotBlank(message = "Confirme a nova senha.")
    @Size(max = 72, message = "A confirmação da senha ultrapassou o tamanho permitido.")
    private String confirmacaoNovaSenha;
}
