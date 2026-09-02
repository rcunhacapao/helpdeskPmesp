package pmesp.helpdesk37bpmm.Tecnico.mapper;

import org.springframework.stereotype.Component;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoRespostaDTO;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;

@Component
public class TecnicoMapper {

    // Transformar técnico do banco em resposta da área técnica
    public TecnicoRespostaDTO map(TecnicoModel tecnicoModel) {
        TecnicoRespostaDTO resposta = new TecnicoRespostaDTO();
        resposta.setId(tecnicoModel.getId());
        resposta.setIdentificacao(tecnicoModel.getUsuario().getIdentificacaoCompleta());
        resposta.setRe(tecnicoModel.getUsuario().getRe());
        resposta.setDisponivel(tecnicoModel.isDisponivel());

        return resposta;
    }
}
