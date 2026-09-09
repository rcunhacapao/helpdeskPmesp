package pmesp.helpdesk37bpmm.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class TratadorDeExcecoes {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeExcecoes.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErroDTO> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return criarResposta(HttpStatus.NOT_FOUND, excecao.getCodigo(), excecao.getMessage());
    }

    @ExceptionHandler(RegraDeNegocioException.class)
    public ResponseEntity<RespostaErroDTO> tratarRegraDeNegocio(RegraDeNegocioException excecao) {
        return criarResposta(HttpStatus.BAD_REQUEST, excecao.getCodigo(), excecao.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<RespostaErroDTO> tratarConflito(ConflitoException excecao) {
        return criarResposta(HttpStatus.CONFLICT, excecao.getCodigo(), excecao.getMessage());
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<RespostaErroDTO> tratarAcessoNegado(AcessoNegadoException excecao) {
        return criarResposta(HttpStatus.FORBIDDEN, excecao.getCodigo(), excecao.getMessage());
    }

    // Evita sobrescrita silenciosa quando duas pessoas alteram o mesmo chamado.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<RespostaErroDTO> tratarConflitoDeConcorrencia() {
        return criarResposta(HttpStatus.CONFLICT, "REGISTRO_ALTERADO_POR_OUTRA_ACAO",
                "Este chamado foi alterado por outra ação enquanto você o acessava. Atualize a página e tente novamente.");
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespostaErroDTO> tratarFalhaDeAutenticacao() {
        return criarResposta(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "RE ou senha inválidos.");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaErroDTO> tratarDadosInvalidos() {
        return criarResposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "Os dados enviados estão em um formato inválido.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErroDTO> tratarCamposInvalidos(MethodArgumentNotValidException excecao) {
        String mensagem = excecao.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" "));

        return criarResposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<RespostaErroDTO> tratarParametroObrigatorioAusente() {
        return criarResposta(HttpStatus.BAD_REQUEST, "PARAMETRO_OBRIGATORIO",
                "Informe o parâmetro obrigatório para realizar esta operação.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespostaErroDTO> tratarValorInvalido() {
        return criarResposta(HttpStatus.BAD_REQUEST, "VALOR_INVALIDO",
                "O valor informado não é válido para este campo.");
    }

    // Registra o detalhe no servidor sem expor informações internas na resposta.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErroDTO> tratarErroInesperado(Exception excecao) {
        log.error("Erro inesperado não tratado por um handler específico", excecao);
        return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro interno. Tente novamente mais tarde.");
    }

    private ResponseEntity<RespostaErroDTO> criarResposta(HttpStatus status, String codigo, String mensagem) {
        RespostaErroDTO resposta = new RespostaErroDTO(status.value(), codigo, mensagem, LocalDateTime.now());
        return ResponseEntity.status(status).body(resposta);
    }
}
