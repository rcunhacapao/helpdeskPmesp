package pmesp.helpdesk37bpmm.Autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados enviados para realizar login
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "Informe o RE.")
    private String re;

    @NotBlank(message = "Informe a senha.")
    private String senha;
}
