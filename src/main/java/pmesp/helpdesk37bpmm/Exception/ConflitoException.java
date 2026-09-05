package pmesp.helpdesk37bpmm.Exception;

// Erro usado quando um dado único já está cadastrado no sistema
public class ConflitoException extends ErroDaApiException {

    public ConflitoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
