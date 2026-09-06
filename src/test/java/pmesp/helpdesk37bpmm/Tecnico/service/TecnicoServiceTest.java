package pmesp.helpdesk37bpmm.Tecnico.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pmesp.helpdesk37bpmm.Exception.ConflitoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoDTO;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoRespostaDTO;
import pmesp.helpdesk37bpmm.Tecnico.mapper.TecnicoMapper;
import pmesp.helpdesk37bpmm.Tecnico.repository.TecnicoRepository;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TecnicoServiceTest {

    @Mock
    TecnicoRepository tecnicoRepository;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    TecnicoMapper tecnicoMapper;

    @InjectMocks
    TecnicoService tecnicoService;

    @Test
    void deveInformarQuandoUsuarioDoTecnicoNaoForEncontrado() {
        TecnicoDTO tecnicoDTO = new TecnicoDTO();
        tecnicoDTO.setRe("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> tecnicoService.criar(tecnicoDTO));

        assertEquals("USUARIO_NAO_ENCONTRADO", excecao.getCodigo());
    }


    @Test
    void deveImpedirCadastroDeUsuarioInativoComoTecnico() {
        TecnicoDTO tecnicoDTO = new TecnicoDTO();
        tecnicoDTO.setRe("250861");
        UsuarioModel usuario = new UsuarioModel();
        usuario.setAtivo(false);
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> tecnicoService.criar(tecnicoDTO));

        assertEquals("USUARIO_INATIVO", excecao.getCodigo());
    }


    @Test
    void deveImpedirCadastroDePerfilTecnicoDuplicado() {
        TecnicoDTO tecnicoDTO = new TecnicoDTO();
        tecnicoDTO.setRe("250861");
        UsuarioModel usuario = new UsuarioModel();
        usuario.setAtivo(true);
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(tecnicoRepository.findByUsuario(usuario)).thenReturn(Optional.of(new TecnicoModel()));

        ConflitoException excecao = assertThrows(ConflitoException.class,
                () -> tecnicoService.criar(tecnicoDTO));

        assertEquals("TECNICO_JA_CADASTRADO", excecao.getCodigo());
    }


    @Test
    void deveListarTodosOsTecnicosCadastrados() {
        TecnicoModel tecnico = new TecnicoModel();
        when(tecnicoRepository.findAll()).thenReturn(List.of(tecnico));
        when(tecnicoMapper.map(tecnico)).thenReturn(new TecnicoRespostaDTO());

        List<TecnicoRespostaDTO> resposta = tecnicoService.listarTodosOsTecnicos();

        assertEquals(1, resposta.size());
    }


    @Test
    void devePermitirQueEquipeTecnicaAltereDisponibilidadeDeQualquerTecnico() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setAtivo(true);
        TecnicoModel tecnicoAlvo = new TecnicoModel();
        tecnicoAlvo.setUsuario(usuario);
        when(tecnicoRepository.findByUsuarioRe("250861")).thenReturn(Optional.of(tecnicoAlvo));
        when(tecnicoRepository.save(tecnicoAlvo)).thenReturn(tecnicoAlvo);

        assertDoesNotThrow(() -> tecnicoService.ficarDisponivel("250861"));
        assertTrue(tecnicoAlvo.isDisponivel());
    }
}
