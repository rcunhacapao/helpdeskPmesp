package pmesp.helpdesk37bpmm.Chamado.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoDTO;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Chamado.mapper.ChamadoMapper;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.Chamado.repository.ChamadoRepository;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {

    @Mock
    ChamadoRepository chamadoRepository;
    @Mock
    UsuarioRepository usuarioRepository;
    @Mock
    ChamadoMapper chamadoMapper;
    @Mock
    TecnicoService tecnicoService;

    @InjectMocks
    ChamadoService chamadoService;

    @Test
    void deveExigirProblemaAoAbrirChamado() {
        ChamadoDTO chamadoDTO = criarChamadoValido();
        chamadoDTO.setDescricao("   ");

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> chamadoService.criar(chamadoDTO));

        assertEquals("PROBLEMA_OBRIGATORIO", excecao.getCodigo());
        assertEquals("Informe o problema.", excecao.getMessage());
    }


    @Test
    void deveInformarQuandoUsuarioDoChamadoNaoForEncontrado() {
        ChamadoDTO chamadoDTO = criarChamadoValido();
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> chamadoService.criar(chamadoDTO));

        assertEquals("USUARIO_NAO_ENCONTRADO", excecao.getCodigo());
    }


    @Test
    void deveInformarQuandoChamadoNaoForEncontrado() {
        when(chamadoRepository.findById(99L)).thenReturn(Optional.empty());

        RecursoNaoEncontradoException excecao = assertThrows(RecursoNaoEncontradoException.class,
                () -> chamadoService.buscarPorId(99L));

        assertEquals("CHAMADO_NAO_ENCONTRADO", excecao.getCodigo());
    }


    @Test
    void deveImpedirInicioQuandoTecnicoEstiverIndisponivel() {
        ChamadoModel chamado = new ChamadoModel();
        chamado.setStatus(ChamadoStatus.ABERTO);
        TecnicoModel tecnico = new TecnicoModel();
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));
        when(tecnicoService.buscarPorReComoModel("250861")).thenReturn(Optional.of(tecnico));
        when(tecnicoService.estaDisponivel(tecnico)).thenReturn(false);

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> chamadoService.iniciarAtendimento(1L, "250861"));

        assertEquals("TECNICO_INDISPONIVEL", excecao.getCodigo());
    }


    @Test
    void deveImpedirCancelamentoComMotivoInvalido() {
        ChamadoModel chamado = new ChamadoModel();
        chamado.setStatus(ChamadoStatus.ABERTO);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> chamadoService.cancelarChamado(1L, "NAO_SEI"));

        assertEquals("MOTIVO_CANCELAMENTO_INVALIDO", excecao.getCodigo());
    }


    @Test
    void deveImpedirFinalizacaoDeChamadoForaDeAtendimento() {
        ChamadoModel chamado = new ChamadoModel();
        chamado.setStatus(ChamadoStatus.ABERTO);
        when(chamadoRepository.findById(1L)).thenReturn(Optional.of(chamado));

        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> chamadoService.finalizarAtendimento(1L));

        assertEquals("CHAMADO_NAO_ESTA_EM_ATENDIMENTO", excecao.getCodigo());
    }


    // Criar dados completos para testar somente a regra desejada
    private ChamadoDTO criarChamadoValido() {
        ChamadoDTO chamadoDTO = new ChamadoDTO();
        chamadoDTO.setRe("250861");
        chamadoDTO.setDescricao("Monitor sem imagem.");
        chamadoDTO.setCategoria(ChamadoCategoria.MONITOR);
        chamadoDTO.setLocalAtendimento("Sala de testes");
        chamadoDTO.setPrioridade(ChamadoPrioridade.MEDIA);

        return chamadoDTO;
    }
}
