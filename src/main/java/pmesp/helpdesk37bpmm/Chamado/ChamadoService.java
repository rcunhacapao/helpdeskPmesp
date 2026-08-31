package pmesp.helpdesk37bpmm.Chamado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Usuario.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChamadoService {

    @Autowired
    ChamadoRepository chamadoRepository;

    @Autowired
    UsuarioRepository usuarioRepository;

    // Cadastrar novo chamado usando os dados do ChamadoDTO
    public ChamadoModel criar(ChamadoDTO chamadoDTO) {
        if (chamadoDTO == null || chamadoDTO.getRe() == null
                || chamadoDTO.getDescricao() == null || chamadoDTO.getDescricao().isBlank()
                || chamadoDTO.getLocalAtendimento() == null || chamadoDTO.getLocalAtendimento().isBlank()
                || chamadoDTO.getPrioridade() == null) {
            return null;
        }

        Optional<UsuarioModel> buscarRe = usuarioRepository.findByRe(chamadoDTO.getRe());
        if (buscarRe.isPresent()) {
            UsuarioModel solicitante = buscarRe.get();
            if (solicitante.isAtivo()) {
                ChamadoModel chamadoNovo = new ChamadoModel();

                chamadoNovo.setSolicitante(solicitante);
                chamadoNovo.setDescricao(chamadoDTO.getDescricao());
                chamadoNovo.setLocalAtendimento(chamadoDTO.getLocalAtendimento());
                chamadoNovo.setPrioridade(chamadoDTO.getPrioridade());
                chamadoNovo.setDataAbertura(LocalDateTime.now());
                chamadoNovo.setStatus(ChamadoStatus.ABERTO);
                chamadoNovo.setMotivoCancelamento(null);

                return chamadoRepository.save(chamadoNovo);
            }
        }
        return null;
    }


    // Pesquisar chamado por ID
    public ChamadoModel buscarPorId(Long chamadoId) {
        Optional<ChamadoModel> chamado = chamadoRepository.findById(chamadoId);
        return chamado.orElse(null);
    }


    // Atualizar a [prioridade] do chamado
    public ChamadoModel atualizarPrioridade(Long chamadoId, ChamadoPrioridade prioridade) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);
        if (chamadoAtual.isPresent()) {
            ChamadoModel chamado = chamadoAtual.get();
            if (chamado.getStatus() != ChamadoStatus.CANCELADO && chamado.getDataFinalizacao() == null) {
                chamado.setPrioridade(prioridade);
                return chamadoRepository.save(chamado);
            }
        }
        return null;
    }


    // Iniciar atendimento do chamado
    public ChamadoModel iniciarAtendimento(Long chamadoId) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);

        if (chamadoAtual.isPresent()) {
            ChamadoModel chamado = chamadoAtual.get();

            if (chamado.getStatus() == ChamadoStatus.ABERTO) {
                chamado.setStatus(ChamadoStatus.EM_ATENDIMENTO);
                return chamadoRepository.save(chamado);
            }
        }

        return null;
    }


    // Finalizar atendimento do chamado
    public ChamadoModel finalizarAtendimento(Long chamadoId) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);

        if (chamadoAtual.isPresent()) {
            ChamadoModel chamado = chamadoAtual.get();

            if (chamado.getStatus() == ChamadoStatus.EM_ATENDIMENTO) {
                chamado.finalizarAtendimento();
                chamado.setStatus(ChamadoStatus.FECHADO);
                return chamadoRepository.save(chamado);
            }
        }
        return null;
    }


    // Cancelar apenas chamado que ainda está aberto
    public ChamadoModel cancelarChamado(Long chamadoId, String motivoCancelamento) {
        Optional<ChamadoModel> chamadoAtual = chamadoRepository.findById(chamadoId);
        if (chamadoAtual.isPresent() && motivoCancelamentoValido(motivoCancelamento)) {
            ChamadoModel chamado = chamadoAtual.get();

            if (chamado.getStatus() == ChamadoStatus.ABERTO) {
                chamado.setMotivoCancelamento(motivoCancelamento);
                chamado.setStatus(ChamadoStatus.CANCELADO);
                return chamadoRepository.save(chamado);
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
