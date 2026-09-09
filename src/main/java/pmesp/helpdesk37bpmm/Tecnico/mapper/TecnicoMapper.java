package pmesp.helpdesk37bpmm.Tecnico.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoRespostaDTO;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;

@Mapper(componentModel = "spring")
public interface TecnicoMapper {

    @Mapping(target = "identificacao", source = "usuario.identificacaoCompleta")
    @Mapping(target = "re", source = "usuario.re")
    TecnicoRespostaDTO map(TecnicoModel tecnicoModel);
}
