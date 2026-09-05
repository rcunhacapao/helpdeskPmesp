package pmesp.helpdesk37bpmm.Usuario.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pmesp.helpdesk37bpmm.Exception.ConflitoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServicesTest {

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    pmesp.helpdesk37bpmm.Usuario.mapper.UsuarioMapper usuarioMapper;

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


    @Test
    void deveCadastrarSemEmailComHashDoReETrocaObrigatoria() {
        UsuarioDTO usuarioDTO = criarUsuarioValido();
        usuarioDTO.setEmail("  ");
        UsuarioModel usuario = new UsuarioModel();
        usuario.setRe("250861");

        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.empty());
        when(usuarioMapper.map(usuarioDTO)).thenReturn(usuario);
        when(passwordEncoder.encode("250861")).thenReturn("hash-do-re");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.map(usuario)).thenReturn(new pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO());

        usuarioServices.criar(usuarioDTO);

        assertEquals("hash-do-re", usuario.getSenhaHash());
        assertTrue(usuario.isTrocaSenhaObrigatoria());
        assertNull(usuario.getEmail());
        verify(usuarioRepository, never()).findByEmail(anyString());
    }


    @Test
    void deveResetarSenhaParaHashDoReEExigirNovaTroca() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setRe("250861");
        usuario.setAtivo(true);
        usuario.setSenhaHash("hash-antigo");

        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("250861")).thenReturn("novo-hash-do-re");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);
        when(usuarioMapper.map(usuario)).thenReturn(new pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO());

        usuarioServices.resetarSenha("250861");

        assertEquals("novo-hash-do-re", usuario.getSenhaHash());
        assertTrue(usuario.isTrocaSenhaObrigatoria());
        verify(usuarioRepository).save(usuario);
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
