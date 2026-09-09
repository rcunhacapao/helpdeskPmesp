package pmesp.helpdesk37bpmm.Chamado.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoRespostaDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ResumoChamadosDTO;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoResolvidoPor;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Chamado.mapper.ChamadoMapper;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.Chamado.repository.ChamadoRepository;
import pmesp.helpdesk37bpmm.Exception.AcessoNegadoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;
import pmesp.helpdesk37bpmm.Usuario.ValidadorDeRe;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ChamadoMapper chamadoMapper;
    private final TecnicoService tecnicoService;


    public ChamadoRespostaDTO criar(ChamadoDTO chamadoDTO) {
        // Usuários comuns iniciam pelo fluxo integrado com o Mike IA. Esta validação no
        // backend impede que alguém burle a interface e pule o diagnóstico.
        if (!autenticadoETecnico()) {
            throw new AcessoNegadoException("ABERTURA_DIRETA_RESTRITA_A_TECNICO",
                    "O atendimento deve ser iniciado pela tela de abertura de chamado.");
        }

        validarDadosParaAbrirChamado(chamadoDTO);

        Optional<UsuarioModel> buscarRe = usuarioRepository.findByRe(chamadoDTO.getRe());
        if (buscarRe.isEmpty()) {
            throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO",
                    "Não foi encontrado usuário para o RE informado.");
        }

        UsuarioModel solicitante = buscarRe.get();
        // Usuário inativo não pode abrir novos chamados
        if (!solicitante.isAtivo()) {
            throw new RegraDeNegocioException("USUARIO_INATIVO",
                    "Não é possível abrir chamado para um usuário inativo.");
        }

        // Um usuário comum só pode abrir chamado para si mesmo; só o técnico pode abrir em nome de outro
        UsuarioModel usuarioAutenticado = usuarioAutenticado();
        boolean abrindoParaOutraPessoa = !usuarioAutenticado.getId().equals(solicitante.getId());
        if (abrindoParaOutraPessoa && !autenticadoETecnico()) {
            throw new AcessoNegadoException("ABERTURA_EM_NOME_DE_OUTRO_NAO_PERMITIDA",
                    "Somente um técnico pode abrir um chamado em nome de outra pessoa.");
        }

        TecnicoModel tecnicoResponsavel = tecnicoService.buscarTecnicoDisponivelParaNovoChamado();
        ChamadoModel chamadoNovo = chamadoMapper.map(chamadoDTO);

        chamadoNovo.setSolicitante(solicitante);
        // "Aberto por" só é preenchido quando o técnico registra em nome de outra pessoa
        chamadoNovo.setAbertoPor(abrindoParaOutraPessoa ? usuarioAutenticado : null);
        chamadoNovo.setTecnicoResponsavel(tecnicoResponsavel);
        chamadoNovo.setDataAbertura(LocalDateTime.now());
        chamadoNovo.setDataUltimaInteracao(LocalDateTime.now());
        chamadoNovo.setStatus(ChamadoStatus.ABERTO);
        chamadoNovo.setMotivoCancelamento(null);

        return chamadoMapper.map(chamadoRepository.save(chamadoNovo));
    }

    public ChamadoRespostaDTO buscarPorId(Long chamadoId) {
        ChamadoModel chamado = buscarChamadoPorId(chamadoId);
        garantirAcessoAoChamado(chamado);
        return chamadoMapper.map(chamado);
    }

    public List<ChamadoRespostaDTO> listarChamadosDoUsuarioAutenticado() {
        UsuarioModel usuarioAutenticado = usuarioAutenticado();
        List<ChamadoModel> chamados = chamadoRepository.findBySolicitanteOrderByDataAberturaDesc(usuarioAutenticado);
        List<ChamadoRespostaDTO> resposta = new ArrayList<>();

        for (ChamadoModel chamado : chamados) {
            resposta.add(chamadoMapper.map(chamado));
        }

        return resposta;
    }

    public List<ChamadoRespostaDTO> listarFilaAtendimento() {
        List<ChamadoModel> chamadosAbertos = chamadoRepository.findByStatusOrderByDataAberturaAsc(ChamadoStatus.ABERTO);
        List<ChamadoRespostaDTO> fila = new ArrayList<>();

        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.URGENTE, fila);
        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.ALTA, fila);
        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.MEDIA, fila);
        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.BAIXA, fila);

        return fila;
    }

    public List<ChamadoRespostaDTO> listarChamadosEmAtendimento() {
        List<ChamadoModel> chamadosEmAtendimento = chamadoRepository.findByStatusOrderByDataAberturaAsc(ChamadoStatus.EM_ATENDIMENTO);
        List<ChamadoRespostaDTO> resposta = new ArrayList<>();

        for (ChamadoModel chamado : chamadosEmAtendimento) {
            resposta.add(chamadoMapper.map(chamado));
        }

        return resposta;
    }

    // Contar chamados abertos hoje, nesta semana e neste mês, para os cartões da Central
    // Técnica. Chamados cancelados nunca contam, em nenhum dos três períodos: um chamado
    // cancelado não representa demanda real de atendimento.
    public ResumoChamadosDTO obterResumoDeChamados() {
        LocalDateTime inicioHoje = LocalDate.now().atStartOfDay();
        LocalDateTime inicioSemana = LocalDate.now().with(DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime inicioMes = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime agora = LocalDateTime.now();

        long chamadosHoje = chamadoRepository.countByDataAberturaBetweenAndStatusNot(inicioHoje, agora, ChamadoStatus.CANCELADO);
        long chamadosSemana = chamadoRepository.countByDataAberturaBetweenAndStatusNot(inicioSemana, agora, ChamadoStatus.CANCELADO);
        long chamadosMes = chamadoRepository.countByDataAberturaBetweenAndStatusNot(inicioMes, agora, ChamadoStatus.CANCELADO);

        return new ResumoChamadosDTO(chamadosHoje, chamadosSemana, chamadosMes);
    }

    // Adicionar na fila somente os chamados de uma prioridade
    private void adicionarChamadosDaPrioridadeNaFila(List<ChamadoModel> chamadosAbertos, ChamadoPrioridade prioridade, List<ChamadoRespostaDTO> fila) {
        for (ChamadoModel chamado : chamadosAbertos) {
            if (chamado.getPrioridade() == prioridade) {
                fila.add(chamadoMapper.map(chamado));
            }
        }
    }

    public ChamadoRespostaDTO atualizarPrioridade(Long chamadoId, ChamadoPrioridade prioridade) {
        // Não permitir atualizar sem escolher uma prioridade
        if (prioridade == null) {
            throw new RegraDeNegocioException("PRIORIDADE_OBRIGATORIA", "Selecione a prioridade do chamado.");
        }

        ChamadoModel chamado = buscarChamadoPorId(chamadoId);
        // Não permitir mudança após o chamado ser encerrado
        if (chamado.getStatus() == ChamadoStatus.CANCELADO || chamado.getDataFinalizacao() != null) {
            throw new RegraDeNegocioException("PRIORIDADE_NAO_PODE_SER_ALTERADA",
                    "Não é possível alterar a prioridade de um chamado fechado ou cancelado.");
        }

        chamado.setPrioridade(prioridade);
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }

    public ChamadoRespostaDTO iniciarAtendimento(Long chamadoId, String reTecnico) {
        ChamadoModel chamado = buscarChamadoPorId(chamadoId);
        TecnicoModel tecnico = buscarTecnicoPorRe(reTecnico);

        // Atendimento só pode começar quando o chamado ainda está aberto
        if (chamado.getStatus() != ChamadoStatus.ABERTO) {
            throw new RegraDeNegocioException("CHAMADO_NAO_ESTA_ABERTO",
                    "Somente chamados abertos podem ter o atendimento iniciado.");
        }

        // Técnico precisa estar disponível para receber o chamado
        if (!tecnicoService.estaDisponivel(tecnico)) {
            throw new RegraDeNegocioException("TECNICO_INDISPONIVEL",
                    "O técnico precisa estar disponível para iniciar um atendimento.");
        }

        // Impedir que outro técnico inicie um chamado já atribuído
        if (chamado.getTecnicoResponsavel() != null
                && !chamado.getTecnicoResponsavel().getId().equals(tecnico.getId())) {
            throw new RegraDeNegocioException("CHAMADO_ATRIBUIDO_A_OUTRO_TECNICO",
                    "Este chamado já está atribuído a outro técnico. Use a transferência de responsável.");
        }

        chamado.setTecnicoResponsavel(tecnico);
        chamado.setStatus(ChamadoStatus.EM_ATENDIMENTO);
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }

    public ChamadoRespostaDTO transferirResponsavel(Long chamadoId, String reTecnico) {
        ChamadoModel chamado = buscarChamadoPorId(chamadoId);
        TecnicoModel tecnico = buscarTecnicoPorRe(reTecnico);

        // Só permitir transferência enquanto o chamado ainda precisa de atendimento
        if (chamado.getStatus() != ChamadoStatus.ABERTO
                && chamado.getStatus() != ChamadoStatus.EM_ATENDIMENTO) {
            throw new RegraDeNegocioException("TRANSFERENCIA_NAO_PERMITIDA",
                    "Somente chamados abertos ou em atendimento podem ser transferidos.");
        }

        // Novo responsável precisa estar disponível
        if (!tecnicoService.estaDisponivel(tecnico)) {
            throw new RegraDeNegocioException("TECNICO_INDISPONIVEL",
                    "O técnico precisa estar disponível para receber um chamado.");
        }

        chamado.setTecnicoResponsavel(tecnico);
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }

    // Finalizar atendimento do chamado. A solução é opcional: o técnico pode registrar
    // o que foi feito, mas isso não impede a finalização quando não for informada.
    public ChamadoRespostaDTO finalizarAtendimento(Long chamadoId, String solucao) {
        ChamadoModel chamado = buscarChamadoPorId(chamadoId);

        // Só finalizar chamado que já começou a ser atendido
        if (chamado.getStatus() != ChamadoStatus.EM_ATENDIMENTO) {
            throw new RegraDeNegocioException("CHAMADO_NAO_ESTA_EM_ATENDIMENTO",
                    "Somente chamados em atendimento podem ser finalizados.");
        }

        chamado.finalizarAtendimento();
        chamado.setStatus(ChamadoStatus.FECHADO);
        chamado.setResolvidoPor(ChamadoResolvidoPor.TECNICO);
        if (solucao != null && !solucao.isBlank()) {
            chamado.setSolucao(solucao);
        }
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }

    public ChamadoRespostaDTO cancelarChamado(Long chamadoId, String motivoCancelamento) {
        ChamadoModel chamado = buscarChamadoPorId(chamadoId);
        garantirAcessoAoChamado(chamado);

        // Aceitar somente os motivos de cancelamento definidos pelo sistema
        if (!motivoCancelamentoValido(motivoCancelamento)) {
            throw new RegraDeNegocioException("MOTIVO_CANCELAMENTO_INVALIDO",
                    "Informe um motivo de cancelamento válido.");
        }

        // Usuário só pode cancelar chamado que ainda não foi atendido
        if (chamado.getStatus() != ChamadoStatus.ABERTO) {
            throw new RegraDeNegocioException("CANCELAMENTO_NAO_PERMITIDO",
                    "Somente chamados abertos podem ser cancelados.");
        }

        chamado.setMotivoCancelamento(motivoCancelamento);
        chamado.setStatus(ChamadoStatus.CANCELADO);
        chamado.registrarInteracao();
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }

    private boolean motivoCancelamentoValido(String motivoCancelamento) {
        return "RESOLVIDO_NO_LOCAL".equals(motivoCancelamento)
                || "NAO_HA_MAIS_NECESSIDADE".equals(motivoCancelamento)
                || "CHAMADO_DUPLICADO".equals(motivoCancelamento)
                || "OUTRO".equals(motivoCancelamento);
    }

    // Cancelar chamados abertos quando o usuário for inativado
    public void cancelarChamadosAbertosDoUsuario(UsuarioModel usuario) {
        List<ChamadoModel> chamadosAbertos = chamadoRepository.findBySolicitanteAndStatus(usuario, ChamadoStatus.ABERTO);

        // Cancelar um por um para registrar o motivo de forma correta
        for (ChamadoModel chamado : chamadosAbertos) {
            chamado.setMotivoCancelamento("USUARIO_INATIVADO");
            chamado.setStatus(ChamadoStatus.CANCELADO);
            chamado.registrarInteracao();
            chamadoRepository.save(chamado);
        }
    }

    // Verificar se técnico ainda possui chamados para atender
    public boolean temChamadosPendentesDoTecnico(TecnicoModel tecnico) {
        List<ChamadoModel> chamadosAbertos = chamadoRepository.findByTecnicoResponsavelAndStatus(tecnico, ChamadoStatus.ABERTO);
        List<ChamadoModel> chamadosEmAtendimento = chamadoRepository.findByTecnicoResponsavelAndStatus(tecnico, ChamadoStatus.EM_ATENDIMENTO);

        return !chamadosAbertos.isEmpty() || !chamadosEmAtendimento.isEmpty();
    }

    private void validarDadosParaAbrirChamado(ChamadoDTO chamadoDTO) {
        if (chamadoDTO == null) {
            throw new RegraDeNegocioException("DADOS_CHAMADO_INVALIDOS",
                    "Envie os dados necessários para abrir o chamado.");
        }

        ValidadorDeRe.validar(chamadoDTO.getRe());

        if (chamadoDTO.getDescricao() == null || chamadoDTO.getDescricao().isBlank()) {
            throw new RegraDeNegocioException("PROBLEMA_OBRIGATORIO", "Informe o problema.");
        }

        if (chamadoDTO.getCategoria() == null) {
            throw new RegraDeNegocioException("CATEGORIA_OBRIGATORIA",
                    "Selecione uma categoria para o chamado.");
        }

        if (chamadoDTO.getLocalAtendimento() == null || chamadoDTO.getLocalAtendimento().isBlank()) {
            throw new RegraDeNegocioException("LOCAL_ATENDIMENTO_OBRIGATORIO", "Informe o local de atendimento.");
        }

        if (chamadoDTO.getPrioridade() == null) {
            throw new RegraDeNegocioException("PRIORIDADE_OBRIGATORIA", "Selecione a prioridade do chamado.");
        }
    }

    private ChamadoModel buscarChamadoPorId(Long chamadoId) {
        Optional<ChamadoModel> chamado = chamadoRepository.findById(chamadoId);

        if (chamado.isPresent()) {
            return chamado.get();
        }

        throw new RecursoNaoEncontradoException("CHAMADO_NAO_ENCONTRADO", "Chamado não encontrado.");
    }

    private TecnicoModel buscarTecnicoPorRe(String reTecnico) {
        Optional<TecnicoModel> tecnico = tecnicoService.buscarPorReComoModel(reTecnico);

        if (tecnico.isPresent()) {
            return tecnico.get();
        }

        throw new RecursoNaoEncontradoException("TECNICO_NAO_ENCONTRADO", "Técnico não encontrado.");
    }

    // Um usuário comum só pode acessar os próprios chamados; o técnico pode acessar todos.
    // Fica centralizado aqui para que, quando existir separação por equipe/unidade no futuro,
    // baste ajustar esta regra em um único lugar.
    private void garantirAcessoAoChamado(ChamadoModel chamado) {
        if (autenticadoETecnico()) {
            return;
        }

        UsuarioModel usuarioAutenticado = usuarioAutenticado();
        if (!chamado.getSolicitante().getId().equals(usuarioAutenticado.getId())) {
            throw new AcessoNegadoException("CHAMADO_DE_OUTRO_USUARIO", "Você não tem acesso a este chamado.");
        }
    }

    private UsuarioModel usuarioAutenticado() {
        String re = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));
    }

    private boolean autenticadoETecnico() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_TECNICO"));
    }
}
