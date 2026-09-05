package pmesp.helpdesk37bpmm.Autenticacao.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Dados devolvidos depois de um login com sucesso
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRespostaDTO {

    private String identificacaoCompleta;
    private String re;
    private boolean tecnico;
    private boolean trocaSenhaObrigatoria;
}
