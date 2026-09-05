package pmesp.helpdesk37bpmm.Exception;

// Erro usado quando o usuário autenticado não tem permissão para acessar o recurso pedido
public class AcessoNegadoException extends ErroDaApiException {

    public AcessoNegadoException(String codigo, String mensagem) {
        super(codigo, mensagem);
    }
}
