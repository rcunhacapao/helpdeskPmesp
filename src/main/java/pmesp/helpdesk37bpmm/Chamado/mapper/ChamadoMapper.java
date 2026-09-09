package pmesp.helpdesk37bpmm.Chamado.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoRespostaDTO;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;

@Mapper(componentModel = "spring")
public interface ChamadoMapper {

    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "descricao", source = "descricao")
    @Mapping(target = "categoria", source = "categoria")
    @Mapping(target = "localAtendimento", source = "localAtendimento")
    @Mapping(target = "prioridade", source = "prioridade")
    ChamadoModel map(ChamadoDTO chamadoDTO);

    @Mapping(target = "solicitante", source = "solicitante.identificacaoCompleta")
    @Mapping(target = "abertoPor", source = "abertoPor.identificacaoCompleta")
    @Mapping(target = "tecnicoResponsavel", source = "tecnicoResponsavel.usuario.identificacaoCompleta")
    ChamadoRespostaDTO map(ChamadoModel chamadoModel);
}
