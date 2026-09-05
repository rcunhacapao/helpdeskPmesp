package pmesp.helpdesk37bpmm.Exception;

// Erro usado quando um dado procurado não existe no sistema
public class RecursoNaoEncontradoException extends RuntimeException {

    // Código que identifica qual dado não foi encontrado
    private final String codigo;

    // Criar o erro com um código para a API e uma mensagem para o usuário
    public RecursoNaoEncontradoException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    // Permitir que o tratador de erros leia o código
    public String getCodigo() {
        return codigo;
    }
}
