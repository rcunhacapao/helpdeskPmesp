package pmesp.helpdesk37bpmm.MikeIA.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pmesp.helpdesk37bpmm.MikeIA.dto.AtendimentoMikeIARespostaDTO;
import pmesp.helpdesk37bpmm.MikeIA.model.AtendimentoMikeIA;

@Mapper(componentModel = "spring")
public interface AtendimentoMikeIAMapper {

    @Mapping(target = "atendimentoId", source = "id")
    @Mapping(target = "chamadoId", source = "chamado.id")
    @Mapping(target = "sugestoes", source = "sugestoesApresentadas")
    AtendimentoMikeIARespostaDTO map(AtendimentoMikeIA atendimento);
}
