package pmesp.helpdesk37bpmm.Exception;

// Erro usado quando uma regra do sistema não permite a ação solicitada
public class RegraDeNegocioException extends ErroDaApiException {

    public RegraDeNegocioException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
