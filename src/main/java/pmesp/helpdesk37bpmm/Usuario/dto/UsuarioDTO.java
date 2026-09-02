package pmesp.helpdesk37bpmm.Usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;

// Dados necessários para cadastrar um novo usuário
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    private UsuarioPostoGraduacao postoGraduacao;
    private String nome;
    private String re;
}
