package pmesp.helpdesk37bpmm.RelatoErro.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatoErroRespostaDTO;
import pmesp.helpdesk37bpmm.RelatoErro.model.RelatoDeErro;

@Mapper(componentModel = "spring")
public interface RelatoErroMapper {

    @Mapping(target = "identificacaoDeQuemRelatou", source = "usuario.identificacaoCompleta")
    RelatoErroRespostaDTO map(RelatoDeErro relato);
}
