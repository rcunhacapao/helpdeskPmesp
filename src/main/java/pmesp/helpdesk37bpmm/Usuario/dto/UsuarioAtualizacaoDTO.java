package pmesp.helpdesk37bpmm.Usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;

// Dados permitidos para atualizar em um usuario ja cadastrado
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAtualizacaoDTO {

    @NotNull(message = "Informe o posto ou graduação do usuário.")
    private UsuarioPostoGraduacao postoGraduacao;

    @NotBlank(message = "Informe o nome do usuário.")
    @Size(max = 120, message = "O nome do usuário ultrapassou o tamanho permitido.")
    private String nome;
}
