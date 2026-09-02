package pmesp.helpdesk37bpmm.Usuario;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados de um usuario que podem ser mostrados como resposta da API
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRespostaDTO {

    private Long id;
    private UsuarioPostoGraduacao postoGraduacao;
    private String nome;
    private String re;
    private boolean ativo;
}
