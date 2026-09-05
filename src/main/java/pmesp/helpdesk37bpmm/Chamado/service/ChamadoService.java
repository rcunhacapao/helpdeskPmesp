package pmesp.helpdesk37bpmm.Chamado.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoRespostaDTO;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChamadoService {

    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ChamadoMapper chamadoMapper;
    @Autowired
    private TecnicoService tecnicoService;

    // Cadastrar novo chamado usando os dados do ChamadoDTO
    public ChamadoRespostaDTO criar(ChamadoDTO chamadoDTO) {
        // Conferir se todos os dados obrigatórios foram enviados
        validarDadosParaAbrirChamado(chamadoDTO);

        // Encontrar quem está abrindo o chamado
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

        // Definir responsável automático somente quando houver um técnico disponível
        TecnicoModel tecnicoResponsavel = tecnicoService.buscarTecnicoDisponivelParaNovoChamado();
        ChamadoModel chamadoNovo = chamadoMapper.map(chamadoDTO);

        // Definir os dados que são regras do sistema
        chamadoNovo.setSolicitante(solicitante);
        // "Aberto por" só é preenchido quando o técnico registra em nome de outra pessoa
        chamadoNovo.setAbertoPor(abrindoParaOutraPessoa ? usuarioAutenticado : null);
        chamadoNovo.setTecnicoResponsavel(tecnicoResponsavel);
        chamadoNovo.setDataAbertura(LocalDateTime.now());
        chamadoNovo.setStatus(ChamadoStatus.ABERTO);
        chamadoNovo.setMotivoCancelamento(null);

        // Transformar o chamado salvo em resposta para a API
        return chamadoMapper.map(chamadoRepository.save(chamadoNovo));
    }


    // Pesquisar chamado por ID
    public ChamadoRespostaDTO buscarPorId(Long chamadoId) {
        ChamadoModel chamado = buscarChamadoPorId(chamadoId);
        garantirAcessoAoChamado(chamado);
        return chamadoMapper.map(chamado);
    }


    // Listar todos os chamados do usuário autenticado (tela "Meus chamados")
    public List<ChamadoRespostaDTO> listarChamadosDoUsuarioAutenticado() {
        UsuarioModel usuarioAutenticado = usuarioAutenticado();
        List<ChamadoModel> chamados = chamadoRepository.findBySolicitanteOrderByDataAberturaDesc(usuarioAutenticado);
        List<ChamadoRespostaDTO> resposta = new ArrayList<>();

        for (ChamadoModel chamado : chamados) {
            resposta.add(chamadoMapper.map(chamado));
        }

        return resposta;
    }


    // Mostrar chamados abertos na ordem correta da fila de atendimento
    public List<ChamadoRespostaDTO> listarFilaAtendimento() {
        List<ChamadoModel> chamadosAbertos = chamadoRepository.findByStatusOrderByDataAberturaAsc(ChamadoStatus.ABERTO);
        List<ChamadoRespostaDTO> fila = new ArrayList<>();

        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.URGENTE, fila);
        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.ALTA, fila);
        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.MEDIA, fila);
        adicionarChamadosDaPrioridadeNaFila(chamadosAbertos, ChamadoPrioridade.BAIXA, fila);

        return fila;
    }


    // Mostrar chamados que já estão sendo atendidos
    public List<ChamadoRespostaDTO> listarChamadosEmAtendimento() {
        List<ChamadoModel> chamadosEmAtendimento = chamadoRepository.findByStatusOrderByDataAberturaAsc(ChamadoStatus.EM_ATENDIMENTO);
        List<ChamadoRespostaDTO> resposta = new ArrayList<>();

        for (ChamadoModel chamado : chamadosEmAtendimento) {
            resposta.add(chamadoMapper.map(chamado));
        }

        return resposta;
    }


    // Adicionar na fila somente os chamados de uma prioridade
    private void adicionarChamadosDaPrioridadeNaFila(List<ChamadoModel> chamadosAbertos,
                                                     ChamadoPrioridade prioridade, List<ChamadoRespostaDTO> fila) {
        for (ChamadoModel chamado : chamadosAbertos) {
            if (chamado.getPrioridade() == prioridade) {
                fila.add(chamadoMapper.map(chamado));
            }
        }
    }


    // Atualizar a prioridade do chamado
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


    // Iniciar atendimento e definir o técnico responsável pelo chamado
    public ChamadoRespostaDTO iniciarAtendimento(Long chamadoId, String reTecnico) {
        // Encontrar o chamado e o técnico que vai iniciar o atendimento
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

        // Registrar o responsável e alterar o status para em atendimento
        chamado.setTecnicoResponsavel(tecnico);
        chamado.setStatus(ChamadoStatus.EM_ATENDIMENTO);
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }


    // Transferir chamado para outro técnico ativo
    public ChamadoRespostaDTO transferirResponsavel(Long chamadoId, String reTecnico) {
        // Encontrar o chamado e o novo técnico responsável
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

        // Registrar a data final e marcar o chamado como fechado
        chamado.finalizarAtendimento();
        chamado.setStatus(ChamadoStatus.FECHADO);
        if (solucao != null && !solucao.isBlank()) {
            chamado.setSolucao(solucao);
        }
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }


    // Cancelar apenas chamado que ainda está aberto
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
        return chamadoMapper.map(chamadoRepository.save(chamado));
    }


    // Motivos aceitos para cancelamento
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
            chamadoRepository.save(chamado);
        }
    }


    // Verificar se técnico ainda possui chamados para atender
    public boolean temChamadosPendentesDoTecnico(TecnicoModel tecnico) {
        List<ChamadoModel> chamadosAbertos = chamadoRepository.findByTecnicoResponsavelAndStatus(tecnico, ChamadoStatus.ABERTO);
        List<ChamadoModel> chamadosEmAtendimento = chamadoRepository.findByTecnicoResponsavelAndStatus(tecnico, ChamadoStatus.EM_ATENDIMENTO);

        return !chamadosAbertos.isEmpty() || !chamadosEmAtendimento.isEmpty();
    }


    // Validar os dados obrigatórios para abrir um chamado
    private void validarDadosParaAbrirChamado(ChamadoDTO chamadoDTO) {
        // Conferir se o corpo do cadastro foi enviado
        if (chamadoDTO == null) {
            throw new RegraDeNegocioException("DADOS_CHAMADO_INVALIDOS",
                    "Envie os dados necessários para abrir o chamado.");
        }

        // Conferir cada campo que a pessoa precisa informar
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


    // Buscar chamado e informar quando ele não existir
    private ChamadoModel buscarChamadoPorId(Long chamadoId) {
        Optional<ChamadoModel> chamado = chamadoRepository.findById(chamadoId);

        if (chamado.isPresent()) {
            return chamado.get();
        }

        throw new RecursoNaoEncontradoException("CHAMADO_NAO_ENCONTRADO", "Chamado não encontrado.");
    }


    // Buscar técnico pelo RE e informar quando ele não existir
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


    // Descobrir, pela sessão autenticada, qual usuário está fazendo a requisição
    private UsuarioModel usuarioAutenticado() {
        String re = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));
    }


    // Confirmar se quem está autenticado tem o perfil de técnico
    private boolean autenticadoETecnico() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_TECNICO"));
    }


    // TODO: antes de abrir chamado, oferecer chatbot com orientações e opções pré-definidas.

}
