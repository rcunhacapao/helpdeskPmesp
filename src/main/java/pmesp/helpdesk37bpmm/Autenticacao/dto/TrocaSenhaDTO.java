package pmesp.helpdesk37bpmm.Autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Senha digitada duas vezes durante a troca obrigatória
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrocaSenhaDTO {

    @NotBlank(message = "Informe a nova senha.")
    private String novaSenha;

    @NotBlank(message = "Confirme a nova senha.")
    private String confirmacaoNovaSenha;
}
