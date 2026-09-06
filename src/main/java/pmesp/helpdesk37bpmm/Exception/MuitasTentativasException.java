package pmesp.helpdesk37bpmm.Exception;

// Indica que uma origem repetiu tentativas de autenticação além do limite seguro.
public class MuitasTentativasException extends RuntimeException {

    public MuitasTentativasException() {
        super("Muitas tentativas. Aguarde um minuto e tente novamente.");
    }
}
