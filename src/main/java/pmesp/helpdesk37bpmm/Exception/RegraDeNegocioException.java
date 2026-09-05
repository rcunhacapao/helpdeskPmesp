package pmesp.helpdesk37bpmm.Exception;

// Erro usado quando uma regra do sistema não permite a ação solicitada
public class RegraDeNegocioException extends RuntimeException {

    // Código que identifica a regra que impediu a ação
    private final String codigo;

    // Criar o erro com um código para a API e uma mensagem para o usuário
    public RegraDeNegocioException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    // Permitir que o tratador de erros leia o código
    public String getCodigo() {
        return codigo;
    }
}
