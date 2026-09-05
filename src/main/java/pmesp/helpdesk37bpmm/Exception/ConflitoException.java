package pmesp.helpdesk37bpmm.Exception;

// Erro usado quando um dado único já está cadastrado no sistema
public class ConflitoException extends RuntimeException {

    // Código que identifica o dado que já existe no sistema
    private final String codigo;

    // Criar o erro com um código para a API e uma mensagem para o usuário
    public ConflitoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    // Permitir que o tratador de erros leia o código
    public String getCodigo() {
        return codigo;
    }
}
