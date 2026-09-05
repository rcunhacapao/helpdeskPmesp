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
    UsuarioRepository usuarioRespository;

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
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setRe("250861");
        usuarioDTO.setNome("Ruan");
        usuarioDTO.setPostoGraduacao(UsuarioPostoGraduacao.SD);
        when(usuarioRespository.findByRe("250861")).thenReturn(Optional.of(new UsuarioModel()));

        ConflitoException excecao = assertThrows(ConflitoException.class,
                () -> usuarioServices.criar(usuarioDTO));

        assertEquals("RE_JA_CADASTRADO", excecao.getCodigo());
    }


    @Test
    void deveInformarQuandoUsuarioNaoForEncontrado() {
        when(usuarioRespository.findByRe("999999")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> usuarioServices.buscarPorRe("999999"));

        assertEquals("USUARIO_NAO_ENCONTRADO", excecao.getCodigo());
    }
}
