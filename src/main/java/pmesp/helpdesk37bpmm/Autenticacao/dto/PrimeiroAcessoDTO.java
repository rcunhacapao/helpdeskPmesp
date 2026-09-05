package pmesp.helpdesk37bpmm.Autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados enviados para confirmar identidade e criar a senha no primeiro acesso
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrimeiroAcessoDTO {

    @NotBlank(message = "Informe o RE.")
    private String re;

    @NotBlank(message = "Informe o e-mail funcional.")
    private String email;

    @NotBlank(message = "Informe a nova senha.")
    private String novaSenha;
}
