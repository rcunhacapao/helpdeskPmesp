package pmesp.helpdesk37bpmm.Exception;

// Erro usado quando um dado procurado não existe no sistema
public class RecursoNaoEncontradoException extends ErroDaApiException {

    public RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
