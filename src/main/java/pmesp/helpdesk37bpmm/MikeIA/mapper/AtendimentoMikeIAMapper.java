package pmesp.helpdesk37bpmm.MikeIA.mapper;

import org.springframework.stereotype.Component;
import pmesp.helpdesk37bpmm.MikeIA.dto.AtendimentoMikeIARespostaDTO;
import pmesp.helpdesk37bpmm.MikeIA.model.AtendimentoMikeIA;

@Component
public class AtendimentoMikeIAMapper {

    // Transformar um atendimento do banco em resposta para a API
    public AtendimentoMikeIARespostaDTO map(AtendimentoMikeIA atendimento) {
        AtendimentoMikeIARespostaDTO resposta = new AtendimentoMikeIARespostaDTO();
        resposta.setAtendimentoId(atendimento.getId());
        // Registros novos sempre possuem chamado. A verificação evita que um dado
        // antigo, criado antes da integração, impeça a consulta do restante do histórico.
        if (atendimento.getChamado() != null) {
            resposta.setChamadoId(atendimento.getChamado().getId());
        }
        resposta.setDescricaoProblema(atendimento.getDescricaoProblema());
        resposta.setCategoria(atendimento.getCategoria());
        resposta.setSugestoes(atendimento.getSugestoesApresentadas());
        resposta.setPossuiOrientacaoTestavel(atendimento.isPossuiOrientacaoTestavel());
        resposta.setResultado(atendimento.getResultado());
        resposta.setDataInicio(atendimento.getDataInicio());
        resposta.setDataConclusao(atendimento.getDataConclusao());

        return resposta;
    }
}
