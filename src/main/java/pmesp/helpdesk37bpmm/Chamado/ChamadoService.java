package pmesp.helpdesk37bpmm.Chamado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Usuario.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChamadoService {

    @Autowired
    ChamadoRepository chamadoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    ChamadoMapper chamadoMapper;

    // Cadastrar novo chamado usando os dados do ChamadoDTO
    public ChamadoRespostaDTO criar(ChamadoDTO chamadoDTO) {
        if (chamadoDTO == null || chamadoDTO.getRe() == null
                || chamadoDTO.getCategoria() == null
                || chamadoDTO.getDescricao() == null || chamadoDTO.getDescricao().isBlank()
                || chamadoDTO.getLocalAtendimento() == null || chamadoDTO.getLocalAtendimento().isBlank()
                || chamadoDTO.getPrioridade() == null) {
            return null;
        }

        Optional<UsuarioModel> buscarRe = usuarioRepository.findByRe(chamadoDTO.getRe());
        if (buscarRe.isPresent()) {
            UsuarioModel solicitante = buscarRe.get();
            if (solicitante.isAtivo()) {
                ChamadoModel chamadoNovo = chamadoMapper.map(chamadoDTO);

                // Definir os dados que são regras do sistema
                chamadoNovo.setSolicitante(solicitante);
                chamadoNovo.setDataAbertura(LocalDateTime.now());
                chamadoNovo.setStatus(ChamadoStatus.ABERTO);
                chamadoNovo.setMotivoCancelamento(null);

                // Transformar o chamado salvo em resposta para a API
                return chamadoMapper.map(chamadoRepository.save(chamadoNovo));
            }
        }
        return null;
    }


    // Pesquisar chamado por ID
    public ChamadoRespostaDTO buscarPorId(Long chamadoId) {
        Optional<ChamadoModel> chamado = chamadoRepository.findById(chamadoId);
        if (chamado.isPresent()) {
            return chamadoMapper.map(chamado.get());
        }
        return null;
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


    // Adicionar na fila somente os chamados de uma prioridade
    private void adicionarChamadosDaPrioridadeNaFila(List<ChamadoModel> chamadosAbertos, ChamadoPrioridade prioridade, List<ChamadoRespostaDTO> fila) {
        for (ChamadoModel chamado : chamadosAbertos) {
            if (chamado.getPrioridade() == prioridade) {
                fila.add(chamadoMapper.map(chamado));
            }
        }
    }


    // Atualizar a [prioridade] do chamado
    public ChamadoRespostaDTO atualizarPrioridade(Long chamadoId, ChamadoPrioridade prioridade) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);
        if (chamadoAtual.isPresent()) {
            ChamadoModel chamado = chamadoAtual.get();
            if (chamado.getStatus() != ChamadoStatus.CANCELADO && chamado.getDataFinalizacao() == null) {
                chamado.setPrioridade(prioridade);
                // Transformar o chamado atualizado em resposta para a API
                return chamadoMapper.map(chamadoRepository.save(chamado));
            }
        }
        return null;
    }


    // Iniciar atendimento do chamado
    public ChamadoRespostaDTO iniciarAtendimento(Long chamadoId) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);

        if (chamadoAtual.isPresent()) {
            ChamadoModel chamado = chamadoAtual.get();

            if (chamado.getStatus() == ChamadoStatus.ABERTO) {
                chamado.setStatus(ChamadoStatus.EM_ATENDIMENTO);
                // Transformar o chamado atualizado em resposta para a API
                return chamadoMapper.map(chamadoRepository.save(chamado));
            }
        }

        return null;
    }


    // Finalizar atendimento do chamado
    public ChamadoRespostaDTO finalizarAtendimento(Long chamadoId) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);

        if (chamadoAtual.isPresent()) {
            ChamadoModel chamado = chamadoAtual.get();

            if (chamado.getStatus() == ChamadoStatus.EM_ATENDIMENTO) {
                chamado.finalizarAtendimento();
                chamado.setStatus(ChamadoStatus.FECHADO);
                // Transformar o chamado finalizado em resposta para a API
                return chamadoMapper.map(chamadoRepository.save(chamado));
            }
        }
        return null;
    }


    // Cancelar apenas chamado que ainda está aberto
    public ChamadoRespostaDTO cancelarChamado(Long chamadoId, String motivoCancelamento) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);
        if (chamadoAtual.isPresent() && motivoCancelamentoValido(motivoCancelamento)) {
            ChamadoModel chamado = chamadoAtual.get();

            if (chamado.getStatus() == ChamadoStatus.ABERTO) {
                chamado.setMotivoCancelamento(motivoCancelamento);
                chamado.setStatus(ChamadoStatus.CANCELADO);
                // Transformar o chamado cancelado em resposta para a API
                return chamadoMapper.map(chamadoRepository.save(chamado));
            }
        }
        return null;
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

        for (ChamadoModel chamado : chamadosAbertos) {
            chamado.setMotivoCancelamento("USUARIO_INATIVADO");
            chamado.setStatus(ChamadoStatus.CANCELADO);
            chamadoRepository.save(chamado);
        }
    }


    // TODO: antes de abrir chamado, oferecer chatbot com orientações e opções pré-definidas.
    // TODO: criar fila de chamados por prioridade e informar a posição na fila.
}
