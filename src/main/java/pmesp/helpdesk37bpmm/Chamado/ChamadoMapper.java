package pmesp.helpdesk37bpmm.Chamado;

import org.springframework.stereotype.Component;

@Component
public class ChamadoMapper {

    // Transformar os dados do cadastro em chamado para salvar no banco
    public ChamadoModel map(ChamadoDTO chamadoDTO) {
        ChamadoModel chamadoModel = new ChamadoModel();
        chamadoModel.setDescricao(chamadoDTO.getDescricao());
        chamadoModel.setLocalAtendimento(chamadoDTO.getLocalAtendimento());
        chamadoModel.setPrioridade(chamadoDTO.getPrioridade());

        return chamadoModel;
    }

    // Transformar um chamado do banco em dados para responder na API
    public ChamadoRespostaDTO map(ChamadoModel chamadoModel) {
        ChamadoRespostaDTO chamadoRespostaDTO = new ChamadoRespostaDTO();
        chamadoRespostaDTO.setId(chamadoModel.getId());
        chamadoRespostaDTO.setSolicitante(chamadoModel.getSolicitante().getIdentificacaoCompleta());
        chamadoRespostaDTO.setDescricao(chamadoModel.getDescricao());
        chamadoRespostaDTO.setLocalAtendimento(chamadoModel.getLocalAtendimento());
        chamadoRespostaDTO.setMotivoCancelamento(chamadoModel.getMotivoCancelamento());
        chamadoRespostaDTO.setPrioridade(chamadoModel.getPrioridade());
        chamadoRespostaDTO.setStatus(chamadoModel.getStatus());
        chamadoRespostaDTO.setDataAbertura(chamadoModel.getDataAbertura());
        chamadoRespostaDTO.setDataFinalizacao(chamadoModel.getDataFinalizacao());

        return chamadoRespostaDTO;
    }
}
