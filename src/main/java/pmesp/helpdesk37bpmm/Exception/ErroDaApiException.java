package pmesp.helpdesk37bpmm.Exception;

// Base comum das exceções de negócio da API: todas carregam um código (para o frontend
// identificar o erro) e uma mensagem (para mostrar ao usuário). Cada subclasse representa
// um tipo de problema diferente, e o TratadorDeExcecoes decide o status HTTP de cada uma.
public abstract class ErroDaApiException extends RuntimeException {

    private final String codigo;

    protected ErroDaApiException(String codigo, String mensagem) {
        super(mensagem);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
