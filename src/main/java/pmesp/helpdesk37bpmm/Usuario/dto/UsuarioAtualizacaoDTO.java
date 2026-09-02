package pmesp.helpdesk37bpmm.Usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;

// Dados permitidos para atualizar em um usuario ja cadastrado
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAtualizacaoDTO {

    private UsuarioPostoGraduacao postoGraduacao;
    private String nome;
}
