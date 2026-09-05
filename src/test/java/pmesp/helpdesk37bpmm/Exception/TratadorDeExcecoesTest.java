package pmesp.helpdesk37bpmm.Exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TratadorDeExcecoesTest {

    @Test
    void deveRetornar404QuandoRecursoNaoForEncontrado() {
        TratadorDeExcecoes tratador = new TratadorDeExcecoes();
        RecursoNaoEncontradoException excecao = new RecursoNaoEncontradoException(
                "USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");

        ResponseEntity<RespostaErroDTO> resposta = tratador.tratarRecursoNaoEncontrado(excecao);

        assertEquals(404, resposta.getStatusCode().value());
        assertEquals("USUARIO_NAO_ENCONTRADO", resposta.getBody().getCodigo());
        assertEquals("Usuário não encontrado.", resposta.getBody().getMensagem());
        assertNotNull(resposta.getBody().getDataHora());
    }


    @Test
    void deveRetornar400QuandoRegraDeNegocioForDescumprida() {
        TratadorDeExcecoes tratador = new TratadorDeExcecoes();
        RegraDeNegocioException excecao = new RegraDeNegocioException(
                "TECNICO_INDISPONIVEL", "O técnico precisa estar disponível para iniciar um atendimento.");

        ResponseEntity<RespostaErroDTO> resposta = tratador.tratarRegraDeNegocio(excecao);

        assertEquals(400, resposta.getStatusCode().value());
        assertEquals("TECNICO_INDISPONIVEL", resposta.getBody().getCodigo());
    }


    @Test
    void deveRetornar409QuandoExistirConflito() {
        TratadorDeExcecoes tratador = new TratadorDeExcecoes();
        ConflitoException excecao = new ConflitoException(
                "RE_JA_CADASTRADO", "Já existe um usuário cadastrado com este RE.");

        ResponseEntity<RespostaErroDTO> resposta = tratador.tratarConflito(excecao);

        assertEquals(409, resposta.getStatusCode().value());
        assertEquals("RE_JA_CADASTRADO", resposta.getBody().getCodigo());
    }
}
