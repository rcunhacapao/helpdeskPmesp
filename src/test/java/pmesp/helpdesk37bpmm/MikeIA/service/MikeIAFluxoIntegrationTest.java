package pmesp.helpdesk37bpmm.MikeIA.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.Chamado.repository.ChamadoRepository;
import pmesp.helpdesk37bpmm.MikeIA.dto.AtendimentoMikeIARespostaDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.EncaminharChamadoDoMikeDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.IniciarAtendimentoMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.enums.AtendimentoMikeIAResultado;
import pmesp.helpdesk37bpmm.MikeIA.repository.AtendimentoMikeIARepository;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

// Exercita os componentes reais do Spring e do JPA no mesmo fluxo que a tela executa.
// O diagnóstico não gera chamado: o registro operacional nasce apenas no encaminhamento.
@SpringBootTest
@TestPropertySource(properties = {
        "DATABASE_URL=jdbc:h2:mem:helpdesk_mike_fluxo_test;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD="
})
class MikeIAFluxoIntegrationTest {

    @Autowired
    private MikeIAService mikeIAService;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private AtendimentoMikeIARepository atendimentoMikeIARepository;

    @BeforeEach
    void prepararBancoDeTeste() {
        atendimentoMikeIARepository.deleteAll();
        chamadoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @AfterEach
    void limparContextoDeSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveCriarChamadoSomenteQuandoUsuarioEnviarEncaminhamentoParaFilaTecnica() {
        UsuarioModel usuario = criarUsuarioDeTeste();
        autenticarComo(usuario.getRe());

        AtendimentoMikeIARespostaDTO diagnostico = mikeIAService.iniciar(
                new IniciarAtendimentoMikeIADTO(
                        "Teste de integração: impressora não imprime.", ChamadoCategoria.IMPRESSORA));

        assertNull(diagnostico.getChamadoId());
        assertEquals(0, chamadoRepository.count());

        EncaminharChamadoDoMikeDTO dadosDoEncaminhamento = new EncaminharChamadoDoMikeDTO(
                ChamadoCategoria.IMPRESSORA, "Laboratório de QA", ChamadoPrioridade.MEDIA);
        dadosDoEncaminhamento.setResumoAtendimento(
                "TRIAGEM MIKE IA\n\nProblema: Impressora não imprime\nResultado: Problema não resolvido.");
        AtendimentoMikeIARespostaDTO encaminhamento = mikeIAService.encaminharParaEquipeTecnica(
                diagnostico.getAtendimentoId(), dadosDoEncaminhamento);

        ChamadoModel chamado = chamadoRepository.findById(encaminhamento.getChamadoId()).orElseThrow();
        assertNotNull(encaminhamento.getChamadoId());
        assertEquals(ChamadoStatus.ABERTO, chamado.getStatus());
        assertEquals(ChamadoCategoria.IMPRESSORA, chamado.getCategoria());
        assertEquals("Laboratório de QA", chamado.getLocalAtendimento());
        assertEquals(ChamadoPrioridade.MEDIA, chamado.getPrioridade());
        assertNotNull(atendimentoMikeIARepository.findByChamadoId(chamado.getId()).orElse(null));
    }

    @Test
    void deveConcluirTriagemResolvidaSemCriarChamado() {
        UsuarioModel usuario = criarUsuarioDeTeste();
        autenticarComo(usuario.getRe());

        AtendimentoMikeIARespostaDTO diagnostico = mikeIAService.iniciar(
                new IniciarAtendimentoMikeIADTO(
                        "Computador lento", ChamadoCategoria.COMPUTADOR));

        mikeIAService.concluirComoResolvido(
                diagnostico.getAtendimentoId(),
                "TRIAGEM MIKE IA\n\nProblema: Computador lento\nResultado: Problema resolvido.");

        assertEquals(0, chamadoRepository.count());
        assertNull(atendimentoMikeIARepository.findById(diagnostico.getAtendimentoId()).orElseThrow().getChamado());
        assertEquals(AtendimentoMikeIAResultado.RESOLVIDO,
                atendimentoMikeIARepository.findById(diagnostico.getAtendimentoId()).orElseThrow().getResultado());
    }

    private UsuarioModel criarUsuarioDeTeste() {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setRe("123456");
        usuario.setNome("Usuário de integração");
        usuario.setPostoGraduacao(UsuarioPostoGraduacao.SD);
        usuario.setEmail("usuario.integracao@policiamilitar.sp.gov.br");
        usuario.setAtivo(true);
        usuario.setSenhaHash(usuario.getRe());
        return usuarioRepository.save(usuario);
    }

    private void autenticarComo(String re) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(re, null));
    }
}
