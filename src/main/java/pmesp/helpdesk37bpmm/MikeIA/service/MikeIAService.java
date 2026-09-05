package pmesp.helpdesk37bpmm.MikeIA.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoResolvidoPor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.Chamado.repository.ChamadoRepository;
import pmesp.helpdesk37bpmm.Exception.AcessoNegadoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.MikeIA.dto.AtendimentoMikeIARespostaDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.EncaminharChamadoDoMikeDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.IniciarAtendimentoMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.MetricasMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.enums.AtendimentoMikeIAResultado;
import pmesp.helpdesk37bpmm.MikeIA.mapper.AtendimentoMikeIAMapper;
import pmesp.helpdesk37bpmm.MikeIA.model.AtendimentoMikeIA;
import pmesp.helpdesk37bpmm.MikeIA.repository.AtendimentoMikeIARepository;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.Optional;

// Orquestra o diagnóstico do Mike. O diagnóstico é temporário: só há chamado
// quando o usuário confirma uma resolução ou envia o encaminhamento final.
@Service
public class MikeIAService {

    private static final int HORAS_PARA_CONSIDERAR_ABANDONO = 24;
    private static final String SOLUCAO_REGISTRADA_PELO_MIKE = "Resolvido com orientações do Mike IA.";

    @Autowired private AtendimentoMikeIARepository atendimentoMikeIARepository;
    @Autowired private ChamadoRepository chamadoRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private TecnicoService tecnicoService;
    @Autowired private MotorDeResolucaoMikeIA motorDeResolucao;
    @Autowired private AtendimentoMikeIAMapper atendimentoMikeIAMapper;

    // O primeiro relato não cria chamado: ele apenas inicia o diagnóstico temporário.
    @Transactional
    public AtendimentoMikeIARespostaDTO iniciar(IniciarAtendimentoMikeIADTO dadosDoProblema) {
        validarDescricaoDoProblema(dadosDoProblema);
        garantirQueUsuarioAutenticadoNaoEhTecnico();

        UsuarioModel usuario = usuarioAutenticado();
        impedirNovoDiagnosticoQuandoJaExisteUmEmAndamento(usuario);
        OrientacaoDoMike orientacao = motorDeResolucao.buscarSugestoes(
                dadosDoProblema.getDescricaoProblema(), dadosDoProblema.getCategoria());

        AtendimentoMikeIA atendimento = new AtendimentoMikeIA();
        atendimento.setUsuario(usuario);
        atendimento.setCategoria(dadosDoProblema.getCategoria());
        atendimento.setDescricaoProblema(dadosDoProblema.getDescricaoProblema().trim());
        atendimento.setSugestoesApresentadas(orientacao.sugestoes());
        atendimento.setPossuiOrientacaoTestavel(orientacao.possuiOrientacaoTestavel());
        atendimento.setDataInicio(LocalDateTime.now());

        return atendimentoMikeIAMapper.map(atendimentoMikeIARepository.save(atendimento));
    }

    // Recupera o diagnóstico temporário depois de uma recarga, sem criar duplicidade.
    @Transactional
    public Optional<AtendimentoMikeIARespostaDTO> buscarDiagnosticoEmAndamento() {
        UsuarioModel usuario = usuarioAutenticado();
        encerrarDiagnosticosExpirados();
        return atendimentoMikeIARepository.findFirstByUsuarioAndResultadoIsNullOrderByDataInicioDesc(usuario)
                .map(atendimentoMikeIAMapper::map);
    }

    // Técnicos consultam o relato do Mike somente quando o usuário efetivamente gerou um chamado.
    @Transactional(readOnly = true)
    public Optional<AtendimentoMikeIARespostaDTO> buscarHistoricoDoChamadoParaTecnico(Long chamadoId) {
        if (!autenticadoEhTecnico()) {
            throw new AcessoNegadoException("HISTORICO_MIKE_IA_RESTRITO_A_TECNICO",
                    "Somente técnicos podem consultar o histórico operacional do Mike IA.");
        }
        return atendimentoMikeIARepository.findByChamadoId(chamadoId).map(atendimentoMikeIAMapper::map);
    }

    @Transactional(readOnly = true)
    public MetricasMikeIADTO obterMetricasParaTecnico() {
        if (!autenticadoEhTecnico()) {
            throw new AcessoNegadoException("METRICAS_MIKE_IA_RESTRITAS_A_TECNICO",
                    "Somente técnicos podem consultar as métricas do Mike IA.");
        }

        long totalIniciado = atendimentoMikeIARepository.count();
        long totalResolvido = atendimentoMikeIARepository.countByResultado(AtendimentoMikeIAResultado.RESOLVIDO);
        long totalEncaminhado = atendimentoMikeIARepository.countByResultado(AtendimentoMikeIAResultado.ENCAMINHADO_PARA_CHAMADO);
        long totalAbandonado = atendimentoMikeIARepository.countByResultado(AtendimentoMikeIAResultado.ABANDONADO);
        double taxaResolucao = totalIniciado == 0 ? 0 : (totalResolvido * 100.0) / totalIniciado;
        return new MetricasMikeIADTO(totalIniciado, totalResolvido, totalEncaminhado, totalAbandonado, taxaResolucao);
    }

    // A confirmação cria um chamado já fechado; ele não passa pela fila técnica.
    @Transactional
    public AtendimentoMikeIARespostaDTO concluirComoResolvido(Long atendimentoId) {
        AtendimentoMikeIA atendimento = buscarAtendimentoPorId(atendimentoId);
        garantirAcessoAoAtendimento(atendimento);
        garantirDiagnosticoAindaAberto(atendimento);

        if (!atendimento.isPossuiOrientacaoTestavel()) {
            throw new RegraDeNegocioException("ORIENTACAO_NAO_CONFIRMAVEL",
                    "Conclua o atendimento pelo formulário de encaminhamento.");
        }

        atendimento.setChamado(criarChamadoResolvidoPeloMike(atendimento));
        atendimento.setResultado(AtendimentoMikeIAResultado.RESOLVIDO);
        atendimento.setDataConclusao(LocalDateTime.now());
        return atendimentoMikeIAMapper.map(atendimentoMikeIARepository.save(atendimento));
    }

    // O chamado técnico existe somente após o envio do formulário complementar.
    @Transactional
    public AtendimentoMikeIARespostaDTO encaminharParaEquipeTecnica(Long atendimentoId,
                                                                      EncaminharChamadoDoMikeDTO dadosDoEncaminhamento) {
        AtendimentoMikeIA atendimento = buscarAtendimentoPorId(atendimentoId);
        garantirAcessoAoAtendimento(atendimento);
        garantirDiagnosticoAindaAberto(atendimento);

        atendimento.setChamado(criarChamadoParaEquipeTecnica(atendimento, dadosDoEncaminhamento));
        atendimento.setCategoria(dadosDoEncaminhamento.getCategoria());
        atendimento.setResultado(AtendimentoMikeIAResultado.ENCAMINHADO_PARA_CHAMADO);
        atendimento.setDataConclusao(LocalDateTime.now());
        return atendimentoMikeIAMapper.map(atendimentoMikeIARepository.save(atendimento));
    }

    // Voltar encerra o diagnóstico temporário, sem criar chamado ou movimentar a fila.
    @Transactional
    public void abandonarDiagnostico(Long atendimentoId) {
        AtendimentoMikeIA atendimento = buscarAtendimentoPorId(atendimentoId);
        garantirAcessoAoAtendimento(atendimento);
        garantirDiagnosticoAindaAberto(atendimento);
        atendimento.setResultado(AtendimentoMikeIAResultado.ABANDONADO);
        atendimento.setDataConclusao(LocalDateTime.now());
        atendimentoMikeIARepository.save(atendimento);
    }

    // Diagnósticos esquecidos não ficam pendentes indefinidamente e não geram chamados.
    @Scheduled(fixedDelay = 3_600_000)
    @Transactional
    public void encerrarDiagnosticosExpirados() {
        LocalDateTime limiteDeInatividade = LocalDateTime.now().minusHours(HORAS_PARA_CONSIDERAR_ABANDONO);
        for (AtendimentoMikeIA atendimento : atendimentoMikeIARepository
                .findByResultadoIsNullAndDataInicioBefore(limiteDeInatividade)) {
            atendimento.setResultado(AtendimentoMikeIAResultado.ABANDONADO);
            atendimento.setDataConclusao(LocalDateTime.now());
            atendimentoMikeIARepository.save(atendimento);
        }
    }

    private ChamadoModel criarChamadoResolvidoPeloMike(AtendimentoMikeIA atendimento) {
        ChamadoModel chamado = criarChamadoBase(atendimento);
        chamado.setStatus(ChamadoStatus.FECHADO);
        chamado.setResolvidoPor(ChamadoResolvidoPor.MIKE_IA);
        chamado.setSolucao(SOLUCAO_REGISTRADA_PELO_MIKE);
        chamado.finalizarAtendimento();
        return chamadoRepository.save(chamado);
    }

    private ChamadoModel criarChamadoParaEquipeTecnica(AtendimentoMikeIA atendimento,
                                                        EncaminharChamadoDoMikeDTO dadosDoEncaminhamento) {
        ChamadoModel chamado = criarChamadoBase(atendimento);
        chamado.setCategoria(dadosDoEncaminhamento.getCategoria());
        chamado.setLocalAtendimento(dadosDoEncaminhamento.getLocalAtendimento().trim());
        chamado.setPrioridade(dadosDoEncaminhamento.getPrioridade());
        chamado.setTecnicoResponsavel(tecnicoService.buscarTecnicoDisponivelParaNovoChamado());
        chamado.setStatus(ChamadoStatus.ABERTO);
        chamado.registrarInteracao();
        return chamadoRepository.save(chamado);
    }

    private ChamadoModel criarChamadoBase(AtendimentoMikeIA atendimento) {
        ChamadoModel chamado = new ChamadoModel();
        chamado.setSolicitante(atendimento.getUsuario());
        chamado.setDescricao(atendimento.getDescricaoProblema());
        chamado.setCategoria(atendimento.getCategoria());
        chamado.setDataAbertura(LocalDateTime.now());
        return chamado;
    }

    private void validarDescricaoDoProblema(IniciarAtendimentoMikeIADTO dadosDoProblema) {
        if (dadosDoProblema == null || dadosDoProblema.getDescricaoProblema() == null
                || dadosDoProblema.getDescricaoProblema().isBlank()) {
            throw new RegraDeNegocioException("PROBLEMA_OBRIGATORIO",
                    "Descreva o problema para iniciarmos o atendimento.");
        }
    }

    private void impedirNovoDiagnosticoQuandoJaExisteUmEmAndamento(UsuarioModel usuario) {
        if (atendimentoMikeIARepository.findFirstByUsuarioAndResultadoIsNullOrderByDataInicioDesc(usuario).isPresent()) {
            throw new RegraDeNegocioException("DIAGNOSTICO_EM_ANDAMENTO",
                    "Você já possui um atendimento em andamento. Continue-o antes de iniciar outro.");
        }
    }

    private AtendimentoMikeIA buscarAtendimentoPorId(Long atendimentoId) {
        return atendimentoMikeIARepository.findById(atendimentoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("ATENDIMENTO_MIKE_IA_NAO_ENCONTRADO",
                        "Atendimento do Mike IA não encontrado."));
    }

    private void garantirDiagnosticoAindaAberto(AtendimentoMikeIA atendimento) {
        if (atendimento.getResultado() != null) {
            throw new RegraDeNegocioException("ATENDIMENTO_JA_CONCLUIDO",
                    "Este atendimento já foi concluído ou encaminhado.");
        }
    }

    private void garantirAcessoAoAtendimento(AtendimentoMikeIA atendimento) {
        if (!atendimento.getUsuario().getId().equals(usuarioAutenticado().getId())) {
            throw new AcessoNegadoException("ATENDIMENTO_MIKE_IA_DE_OUTRO_USUARIO",
                    "Você não tem acesso a este atendimento.");
        }
    }

    // A regra usa a role da sessão. Um técnico que abre em nome de outra pessoa continua técnico.
    private void garantirQueUsuarioAutenticadoNaoEhTecnico() {
        if (autenticadoEhTecnico()) {
            throw new AcessoNegadoException("MIKE_IA_NAO_APLICAVEL_A_TECNICO",
                    "Técnicos devem registrar o chamado diretamente pelo formulário completo.");
        }
    }

    private boolean autenticadoEhTecnico() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_TECNICO"));
    }

    private UsuarioModel usuarioAutenticado() {
        String re = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));
    }
}
