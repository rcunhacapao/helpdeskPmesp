package pmesp.helpdesk37bpmm.RelatoErro.mapper;

import org.springframework.stereotype.Component;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatoErroRespostaDTO;
import pmesp.helpdesk37bpmm.RelatoErro.model.RelatoDeErro;

@Component
public class RelatoErroMapper {

    // Transformar relato do banco em resposta da área técnica
    public RelatoErroRespostaDTO map(RelatoDeErro relato) {
        RelatoErroRespostaDTO resposta = new RelatoErroRespostaDTO();
        resposta.setId(relato.getId());
        resposta.setTipoErro(relato.getTipoErro());
        resposta.setObservacao(relato.getObservacao());
        resposta.setIdentificacaoDeQuemRelatou(relato.getUsuario().getIdentificacaoCompleta());
        resposta.setDataRelato(relato.getDataRelato());
        return resposta;
    }
}
