package pmesp.helpdesk37bpmm.Exception;

import jakarta.validation.ConstraintViolationException;
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
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

// Transformar exceções do sistema em respostas claras para a API
@RestControllerAdvice
public class TratadorDeExcecoes {

    private static final Logger log = LoggerFactory.getLogger(TratadorDeExcecoes.class);

    // Responder com 404 quando o dado procurado não existir
    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<RespostaErroDTO> tratarRecursoNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return criarResposta(HttpStatus.NOT_FOUND, excecao.getCodigo(), excecao.getMessage());
    }

    // Uma URL sem controller ou arquivo estático correspondente também é 404. Sem este
    // caso, o handler genérico convertia a ausência do recurso em erro interno 500.
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<RespostaErroDTO> tratarRotaNaoEncontrada() {
        return criarResposta(HttpStatus.NOT_FOUND, "ROTA_NAO_ENCONTRADA",
                "O recurso solicitado não foi encontrado.");
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

    // Responder com 403 quando o usuário autenticado não puder acessar o recurso
    @ExceptionHandler(AcessoNegadoException.class)
    public ResponseEntity<RespostaErroDTO> tratarAcessoNegado(AcessoNegadoException excecao) {
        return criarResposta(HttpStatus.FORBIDDEN, excecao.getCodigo(), excecao.getMessage());
    }

    // Responder com 409 quando dois usuários tentam alterar o mesmo registro ao mesmo
    // tempo (ex.: dois técnicos assumindo o mesmo chamado). Sem isso, o segundo a salvar
    // sobrescreveria o primeiro em silêncio.
    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<RespostaErroDTO> tratarConflitoDeConcorrencia() {
        return criarResposta(HttpStatus.CONFLICT, "REGISTRO_ALTERADO_POR_OUTRA_ACAO",
                "Este chamado foi alterado por outra ação enquanto você o acessava. Atualize a página e tente novamente.");
    }

    // Responder com 401 quando o RE ou a senha do login estiverem incorretos
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespostaErroDTO> tratarFalhaDeAutenticacao() {
        return criarResposta(HttpStatus.UNAUTHORIZED, "CREDENCIAIS_INVALIDAS", "RE ou senha inválidos.");
    }

    // O tempo de espera é informado também no cabeçalho padrão para clientes automatizados.
    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<RespostaErroDTO> tratarMuitasTentativas(MuitasTentativasException excecao) {
        RespostaErroDTO resposta = new RespostaErroDTO(HttpStatus.TOO_MANY_REQUESTS.value(),
                "MUITAS_TENTATIVAS", excecao.getMessage(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .body(resposta);
    }

    // Responder com 400 quando o JSON estiver mal preenchido
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespostaErroDTO> tratarDadosInvalidos() {
        return criarResposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "Os dados enviados estão em um formato inválido.");
    }

    // Responder com 400 quando os campos enviados no corpo da requisição (@Valid) não passarem
    // nas validações estruturais dos DTOs, como campos obrigatórios em branco
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaErroDTO> tratarCamposInvalidos(MethodArgumentNotValidException excecao) {
        String mensagem = excecao.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(" "));

        return criarResposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS", mensagem);
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

    // Validações aplicadas diretamente em parâmetros de rota e query string também
    // precisam produzir um 400 controlado, nunca cair no erro interno genérico.
    @ExceptionHandler({HandlerMethodValidationException.class, ConstraintViolationException.class})
    public ResponseEntity<RespostaErroDTO> tratarParametroForaDosLimites() {
        return criarResposta(HttpStatus.BAD_REQUEST, "DADOS_INVALIDOS",
                "Um dos valores informados não respeita o formato ou o tamanho permitido.");
    }

    // Evitar expor detalhes internos do Java ou do banco de dados na resposta da API,
    // mas registrar o erro completo no log — sem isso, fica impossível descobrir depois
    // o que realmente quebrou (só aparecia "erro interno" para todo mundo).
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaErroDTO> tratarErroInesperado(Exception excecao) {
        log.error("Erro inesperado não tratado por um handler específico", excecao);
        return criarResposta(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO",
                "Ocorreu um erro interno. Tente novamente mais tarde.");
    }

    // Montar todas as respostas de erro no mesmo formato
    private ResponseEntity<RespostaErroDTO> criarResposta(HttpStatus status, String codigo, String mensagem) {
        RespostaErroDTO resposta = new RespostaErroDTO(status.value(), codigo, mensagem, LocalDateTime.now());
        return ResponseEntity.status(status).body(resposta);
    }
}
