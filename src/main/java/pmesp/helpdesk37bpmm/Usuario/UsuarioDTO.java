package pmesp.helpdesk37bpmm.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados necessários para cadastrar um novo usuário
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private UsuarioPostoGraduacao postoGraduacao;
    private String nome;
    private String re;
}
