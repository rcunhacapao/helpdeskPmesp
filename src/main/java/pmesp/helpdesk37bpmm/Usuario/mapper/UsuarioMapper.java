package pmesp.helpdesk37bpmm.Usuario.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "postoGraduacao", source = "postoGraduacao")
    @Mapping(target = "nome", source = "nome")
    @Mapping(target = "re", source = "re")
    @Mapping(target = "email", source = "email")
    UsuarioModel map(UsuarioDTO usuarioDTO);

    UsuarioRespostaDTO map(UsuarioModel usuarioModel);
}
