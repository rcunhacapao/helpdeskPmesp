package pmesp.helpdesk37bpmm.Usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String nome;

    @NotBlank(message = "Informe o RE do usuário.")
    private String re;

    private String email;
}
