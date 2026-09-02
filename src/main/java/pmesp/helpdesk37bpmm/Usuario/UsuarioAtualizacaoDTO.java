package pmesp.helpdesk37bpmm.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados permitidos para atualizar em um usuario ja cadastrado
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioAtualizacaoDTO {

    private UsuarioPostoGraduacao postoGraduacao;
    private String nome;
}
