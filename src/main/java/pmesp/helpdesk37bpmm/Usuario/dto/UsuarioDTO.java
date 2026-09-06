package pmesp.helpdesk37bpmm.Usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;

// Dados necessários para cadastrar um novo usuário
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    @NotNull(message = "Informe o posto ou graduação do usuário.")
    private UsuarioPostoGraduacao postoGraduacao;

    @NotBlank(message = "Informe o nome do usuário.")
    @Size(max = 120, message = "O nome do usuário ultrapassou o tamanho permitido.")
    private String nome;

    @NotBlank(message = "Informe o RE do usuário.")
    @Size(max = 6, message = "O RE ultrapassou o tamanho permitido.")
    private String re;

    @Size(max = 254, message = "O e-mail ultrapassou o tamanho permitido.")
    private String email;
}
