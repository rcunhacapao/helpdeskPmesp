package pmesp.helpdesk37bpmm.Chamado.mapper;

import org.springframework.stereotype.Component;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoRespostaDTO;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;

@Component
public class ChamadoMapper {

    // Transformar os dados do cadastro em chamado para salvar no banco
    public ChamadoModel map(ChamadoDTO chamadoDTO) {
        ChamadoModel chamadoModel = new ChamadoModel();
        chamadoModel.setDescricao(chamadoDTO.getDescricao());
        chamadoModel.setCategoria(chamadoDTO.getCategoria());
        chamadoModel.setLocalAtendimento(chamadoDTO.getLocalAtendimento());
        chamadoModel.setPrioridade(chamadoDTO.getPrioridade());

        return chamadoModel;
    }

    // Transformar um chamado do banco em dados para responder na API
    public ChamadoRespostaDTO map(ChamadoModel chamadoModel) {
        ChamadoRespostaDTO chamadoRespostaDTO = new ChamadoRespostaDTO();
        chamadoRespostaDTO.setId(chamadoModel.getId());
        chamadoRespostaDTO.setSolicitante(chamadoModel.getSolicitante().getIdentificacaoCompleta());
        if (chamadoModel.getTecnicoResponsavel() != null) {
            chamadoRespostaDTO.setTecnicoResponsavel(chamadoModel.getTecnicoResponsavel().getUsuario().getIdentificacaoCompleta());
        }
        chamadoRespostaDTO.setDescricao(chamadoModel.getDescricao());
        chamadoRespostaDTO.setCategoria(chamadoModel.getCategoria());
        chamadoRespostaDTO.setLocalAtendimento(chamadoModel.getLocalAtendimento());
        chamadoRespostaDTO.setMotivoCancelamento(chamadoModel.getMotivoCancelamento());
        chamadoRespostaDTO.setPrioridade(chamadoModel.getPrioridade());
        chamadoRespostaDTO.setStatus(chamadoModel.getStatus());
        chamadoRespostaDTO.setDataAbertura(chamadoModel.getDataAbertura());
        chamadoRespostaDTO.setDataFinalizacao(chamadoModel.getDataFinalizacao());

        return chamadoRespostaDTO;
    }
}
