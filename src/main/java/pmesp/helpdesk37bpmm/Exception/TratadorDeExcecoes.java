package pmesp.helpdesk37bpmm.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

// Transformar exceções do sistema em respostas claras para a API
@RestControllerAdvice
public class TratadorDeExcecoes {

    // Responder com 404 quando o dado procurado não existir
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErroDTO> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return criarResposta(HttpStatus.NOT_FOUND, excecao.getCodigo(), excecao.getMessage());
    }

    // Responder com 400 quando uma regra do sistema não permitir a ação
    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<RespostaErroDTO> tratarRegraDeNegocio(RegraDeNegocioException excecao) {
        return criarResposta(HttpStatus.BAD_REQUEST, excecao.getCodigo(), excecao.getMessage());
    }

    // Responder com 409 quando o cadastro já existir
    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<RespostaErroDTO> tratarConflito(ConflitoException excecao) {
        return criarResposta(HttpStatus.CONFLICT, excecao.getCodigo(), excecao.getMessage());
    }

    // Responder com 400 quando o JSON estiver mal preenchido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaErroDTO> tratarDadosInvalidos() {
        return criarResposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "Os dados enviados estão em um formato inválido.");
    }

    // Responder com 400 quando faltar uma informação na URL
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespostaErroDTO> tratarParametroObrigatorioAusente() {
        return criarResposta(HttpStatus.BAD_REQUEST, "PARAMETRO_OBRIGATORIO",
                "Informe o parâmetro obrigatório para realizar esta operação.");
    }

    // Responder com 400 quando a informação estiver no formato errado
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespostaErroDTO> tratarValorInvalido() {
        return criarResposta(HttpStatus.BAD_REQUEST, "VALOR_INVALIDO",
                "O valor informado não é válido para este campo.");
    }

    // Evitar expor detalhes internos do Java ou do banco de dados
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErroDTO> tratarErroInesperado() {
        return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro interno. Tente novamente mais tarde.");
    }

    // Montar todas as respostas de erro no mesmo formato
    private ResponseEntity<RespostaErroDTO> criarResposta(HttpStatus status, String codigo, String mensagem) {
        RespostaErroDTO resposta = new RespostaErroDTO(status.value(), codigo, mensagem, LocalDateTime.now());
        return ResponseEntity.status(status).body(resposta);
    }
}
