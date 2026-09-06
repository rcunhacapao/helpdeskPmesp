package pmesp.helpdesk37bpmm.Autenticacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados enviados para realizar login
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @NotBlank(message = "Informe o RE.")
    @Pattern(regexp = "[0-9]{1,6}", message = "Informe o RE sem o dígito.")
    private String re;

    @NotBlank(message = "Informe a senha.")
    @Size(max = 72, message = "A senha ultrapassou o tamanho permitido.")
    private String senha;
}
