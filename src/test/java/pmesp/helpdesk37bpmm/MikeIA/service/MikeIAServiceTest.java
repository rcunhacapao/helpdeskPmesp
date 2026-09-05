package pmesp.helpdesk37bpmm.MikeIA.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.Chamado.repository.ChamadoRepository;
import pmesp.helpdesk37bpmm.Exception.AcessoNegadoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.MikeIA.dto.AtendimentoMikeIARespostaDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.EncaminharChamadoDoMikeDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.IniciarAtendimentoMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.enums.AtendimentoMikeIAResultado;
import pmesp.helpdesk37bpmm.MikeIA.mapper.AtendimentoMikeIAMapper;
import pmesp.helpdesk37bpmm.MikeIA.model.AtendimentoMikeIA;
import pmesp.helpdesk37bpmm.MikeIA.repository.AtendimentoMikeIARepository;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MikeIAServiceTest {

    @Mock AtendimentoMikeIARepository atendimentoMikeIARepository;
    @Mock ChamadoRepository chamadoRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock TecnicoService tecnicoService;
    @Mock AtendimentoMikeIAMapper atendimentoMikeIAMapper;
    @InjectMocks MikeIAService mikeIAService;

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveIniciarDiagnosticoSemCriarChamado() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.findFirstByUsuarioAndResultadoIsNullOrderByDataInicioDesc(usuario))
                .thenReturn(Optional.empty());
        when(atendimentoMikeIARepository.save(any(AtendimentoMikeIA.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIAMapper.map(any(AtendimentoMikeIA.class))).thenReturn(new AtendimentoMikeIARespostaDTO());

        mikeIAService.iniciar(new IniciarAtendimentoMikeIADTO("Impressora não imprime", ChamadoCategoria.IMPRESSORA));

        ArgumentCaptor<AtendimentoMikeIA> atendimentoSalvo = ArgumentCaptor.forClass(AtendimentoMikeIA.class);
        verify(atendimentoMikeIARepository).save(atendimentoSalvo.capture());
        assertNull(atendimentoSalvo.getValue().getChamado());
        assertTrue(atendimentoSalvo.getValue().isPossuiOrientacaoTestavel());
        verify(chamadoRepository, never()).save(any(ChamadoModel.class));
    }

    @Test
    void deveImpedirTecnicoDeIniciarDiagnosticoMesmoAbrindoParaOutroRe() {
        autenticarComoTecnico("250861");

        AcessoNegadoException excecao = assertThrows(AcessoNegadoException.class,
                () -> mikeIAService.iniciar(new IniciarAtendimentoMikeIADTO("Impressora não imprime", null)));

        assertEquals("MIKE_IA_NAO_APLICAVEL_A_TECNICO", excecao.getCodigo());
    }

    @Test
    void deveEncerrarAtendimentoSemCriarChamadoQuandoUsuarioConfirmarResolucao() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        AtendimentoMikeIA atendimento = criarDiagnostico(usuario, true);
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(atendimento));
        when(atendimentoMikeIARepository.save(any(AtendimentoMikeIA.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIAMapper.map(any(AtendimentoMikeIA.class))).thenReturn(new AtendimentoMikeIARespostaDTO());

        mikeIAService.concluirComoResolvido(10L, "TRIAGEM MIKE IA\n\nResultado: Problema resolvido.");

        assertNull(atendimento.getChamado());
        assertEquals(AtendimentoMikeIAResultado.RESOLVIDO, atendimento.getResultado());
        verify(chamadoRepository, never()).save(any(ChamadoModel.class));
    }

    @Test
    void deveCriarChamadoAbertoSomenteDepoisDoEnvioDoEncaminhamento() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        AtendimentoMikeIA atendimento = criarDiagnostico(usuario, false);
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(atendimento));
        when(chamadoRepository.save(any(ChamadoModel.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIARepository.save(any(AtendimentoMikeIA.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIAMapper.map(any(AtendimentoMikeIA.class))).thenReturn(new AtendimentoMikeIARespostaDTO());

        EncaminharChamadoDoMikeDTO dto = new EncaminharChamadoDoMikeDTO(
                ChamadoCategoria.IMPRESSORA, "Administração", ChamadoPrioridade.ALTA);
        dto.setResumoAtendimento("TRIAGEM MIKE IA\n\nResultado: Problema não resolvido.");
        mikeIAService.encaminharParaEquipeTecnica(10L, dto);

        assertEquals(ChamadoStatus.ABERTO, atendimento.getChamado().getStatus());
        assertEquals("Administração", atendimento.getChamado().getLocalAtendimento());
        assertEquals(AtendimentoMikeIAResultado.ENCAMINHADO_PARA_CHAMADO, atendimento.getResultado());
        verify(chamadoRepository).save(atendimento.getChamado());
    }

    @Test
    void deveImpedirChamadoDuplicadoQuandoEncaminhamentoForEnviadoNovamente() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        AtendimentoMikeIA atendimento = criarDiagnostico(usuario, false);
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(atendimento));
        when(chamadoRepository.save(any(ChamadoModel.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIARepository.save(any(AtendimentoMikeIA.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIAMapper.map(any(AtendimentoMikeIA.class))).thenReturn(new AtendimentoMikeIARespostaDTO());

        EncaminharChamadoDoMikeDTO dto = new EncaminharChamadoDoMikeDTO(
                ChamadoCategoria.IMPRESSORA, "Administração", ChamadoPrioridade.ALTA);
        dto.setResumoAtendimento("TRIAGEM MIKE IA\n\nResultado: Problema não resolvido.");

        mikeIAService.encaminharParaEquipeTecnica(10L, dto);
        RegraDeNegocioException excecao = assertThrows(RegraDeNegocioException.class,
                () -> mikeIAService.encaminharParaEquipeTecnica(10L, dto));

        assertEquals("ATENDIMENTO_JA_CONCLUIDO", excecao.getCodigo());
        verify(chamadoRepository, times(1)).save(any(ChamadoModel.class));
    }

    @Test
    void deveGuardarResumoDaTriagemSemCriarChamadoAoConcluir() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        AtendimentoMikeIA atendimento = criarDiagnostico(usuario, true);
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(atendimento));
        when(atendimentoMikeIARepository.save(any(AtendimentoMikeIA.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIAMapper.map(any(AtendimentoMikeIA.class))).thenReturn(new AtendimentoMikeIARespostaDTO());

        mikeIAService.concluirComoResolvido(10L, "TRIAGEM MIKE IA\n\nRespostas:\n- Luz acesa: Sim");

        assertEquals("TRIAGEM MIKE IA\n\nRespostas:\n- Luz acesa: Sim", atendimento.getSugestoesApresentadas());
        assertNull(atendimento.getChamado());
        verify(chamadoRepository, never()).save(any(ChamadoModel.class));
    }

    @Test
    void deveAnexarResumoDaTriagemGuiadaNaDescricaoDoChamadoAoEncaminhar() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        AtendimentoMikeIA atendimento = criarDiagnostico(usuario, false);
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(atendimento));
        when(chamadoRepository.save(any(ChamadoModel.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIARepository.save(any(AtendimentoMikeIA.class))).thenAnswer(chamada -> chamada.getArgument(0));
        when(atendimentoMikeIAMapper.map(any(AtendimentoMikeIA.class))).thenReturn(new AtendimentoMikeIARespostaDTO());

        EncaminharChamadoDoMikeDTO dto = new EncaminharChamadoDoMikeDTO(
                ChamadoCategoria.IMPRESSORA, "Administração", ChamadoPrioridade.ALTA);
        dto.setResumoAtendimento("TRIAGEM MIKE IA\n\nResultado: Problema não resolvido.");
        dto.setCpf("12345678901");

        mikeIAService.encaminharParaEquipeTecnica(10L, dto);

        assertTrue(atendimento.getChamado().getDescricao().contains("TRIAGEM MIKE IA"));
        assertTrue(atendimento.getChamado().getDescricao().contains("CPF informado: 12345678901"));
        assertTrue(atendimento.getChamado().getDescricao().startsWith("Impressora não imprime"));
    }

    @Test
    void deveAbandonarDiagnosticoSemCriarChamadoQuandoUsuarioVoltar() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        AtendimentoMikeIA atendimento = criarDiagnostico(usuario, true);
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.buscarPorIdParaAtualizacao(10L)).thenReturn(Optional.of(atendimento));

        mikeIAService.abandonarDiagnostico(10L);

        assertEquals(AtendimentoMikeIAResultado.ABANDONADO, atendimento.getResultado());
        assertNull(atendimento.getChamado());
        verify(chamadoRepository, never()).save(any(ChamadoModel.class));
    }

    @Test
    void deveRecuperarDiagnosticoAposRecarregarPagina() {
        UsuarioModel usuario = criarUsuario(1L, "250861");
        AtendimentoMikeIA atendimento = criarDiagnostico(usuario, true);
        AtendimentoMikeIARespostaDTO respostaMapeada = new AtendimentoMikeIARespostaDTO();
        autenticarComoUsuario("250861");
        when(usuarioRepository.findByRe("250861")).thenReturn(Optional.of(usuario));
        when(atendimentoMikeIARepository.findFirstByUsuarioAndResultadoIsNullOrderByDataInicioDesc(usuario))
                .thenReturn(Optional.of(atendimento));
        when(atendimentoMikeIARepository.findByResultadoIsNullAndDataInicioBefore(any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(atendimentoMikeIAMapper.map(atendimento)).thenReturn(respostaMapeada);

        Optional<AtendimentoMikeIARespostaDTO> resposta = mikeIAService.buscarDiagnosticoEmAndamento();

        assertTrue(resposta.isPresent());
        assertEquals(respostaMapeada, resposta.get());
    }

    @Test
    void devePermitirHistoricoSomenteParaTecnicoQuandoChamadoExistir() {
        AtendimentoMikeIA atendimento = criarDiagnostico(criarUsuario(1L, "250861"), true);
        atendimento.setChamado(new ChamadoModel());
        autenticarComoTecnico("999999");
        when(atendimentoMikeIARepository.findByChamadoId(10L)).thenReturn(Optional.of(atendimento));
        when(atendimentoMikeIAMapper.map(atendimento)).thenReturn(new AtendimentoMikeIARespostaDTO());

        assertTrue(mikeIAService.buscarHistoricoDoChamadoParaTecnico(10L).isPresent());
    }

    private void autenticarComoUsuario(String re) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(re, null, List.of()));
    }

    private void autenticarComoTecnico(String re) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                re, null, List.of(new SimpleGrantedAuthority("ROLE_TECNICO"))));
    }

    private UsuarioModel criarUsuario(Long id, String re) {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setId(id);
        usuario.setRe(re);
        return usuario;
    }

    private AtendimentoMikeIA criarDiagnostico(UsuarioModel usuario, boolean possuiOrientacaoTestavel) {
        AtendimentoMikeIA atendimento = new AtendimentoMikeIA();
        atendimento.setId(10L);
        atendimento.setUsuario(usuario);
        atendimento.setDescricaoProblema("Impressora não imprime");
        atendimento.setCategoria(ChamadoCategoria.IMPRESSORA);
        atendimento.setPossuiOrientacaoTestavel(possuiOrientacaoTestavel);
        atendimento.setDataInicio(LocalDateTime.now());
        return atendimento;
    }
}
