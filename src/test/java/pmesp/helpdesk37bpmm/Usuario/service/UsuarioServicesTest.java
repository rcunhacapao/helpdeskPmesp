package pmesp.helpdesk37bpmm.Usuario.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pmesp.helpdesk37bpmm.Exception.ConflitoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServicesTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @InjectMocks
    UsuarioServices usuarioServices;

    @Test
    void deveInformarQueORePrecisaSerEnviadoSemDigito() {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setRe("250861-1");

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> usuarioServices.criar(usuarioDTO));

        assertEquals("RE_INVALIDO", excecao.getCodigo());
        assertEquals("Informe o RE sem o dígito.", excecao.getMessage());
    }


    @Test
    void deveImpedirCadastroQuandoReJaExistir() {
        UsuarioDTO usuarioDTO = criarUsuarioValido();
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(new UsuarioModel()));

        ConflitoException excecao = assertThrows(ConflitoException.class,
                () -> usuarioServices.criar(usuarioDTO));

        assertEquals("RE_JA_CADASTRADO", excecao.getCodigo());
    }


    @Test
    void deveExigirEmailFuncionalComDominioInstitucional() {
        UsuarioDTO usuarioDTO = criarUsuarioValido();
        usuarioDTO.setEmail("ruan@gmail.com");

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> usuarioServices.criar(usuarioDTO));

        assertEquals("EMAIL_FUNCIONAL_INVALIDO", excecao.getCodigo());
    }


    @Test
    void deveImpedirCadastroQuandoEmailJaExistir() {
        UsuarioDTO usuarioDTO = criarUsuarioValido();
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail("ruan@policiamilitar.sp.gov.br")).thenReturn(Optional.of(new UsuarioModel()));

        ConflitoException excecao = assertThrows(ConflitoException.class,
                () -> usuarioServices.criar(usuarioDTO));

        assertEquals("EMAIL_JA_CADASTRADO", excecao.getCodigo());
    }


    // Criar dados completos para testar somente a regra desejada
    private UsuarioDTO criarUsuarioValido() {
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setRe("250861");
        usuarioDTO.setNome("Ruan");
        usuarioDTO.setPostoGraduacao(UsuarioPostoGraduacao.SD);
        usuarioDTO.setEmail("ruan@policiamilitar.sp.gov.br");
        return usuarioDTO;
    }


    @Test
    void deveInformarQuandoUsuarioNaoForEncontrado() {
        when(usuarioRepository.findByRe("999999")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> usuarioServices.buscarPorRe("999999"));

        assertEquals("USUARIO_NAO_ENCONTRADO", excecao.getCodigo());
    }
}
