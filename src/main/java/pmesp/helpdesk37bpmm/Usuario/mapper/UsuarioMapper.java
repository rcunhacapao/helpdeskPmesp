package pmesp.helpdesk37bpmm.Usuario.mapper;

import org.springframework.stereotype.Component;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

@Component
public class UsuarioMapper {

    // Transformar os dados do cadastro em um usuario para salvar no banco
    public UsuarioModel map(UsuarioDTO usuarioDTO) {
        UsuarioModel usuarioModel = new UsuarioModel();
        usuarioModel.setPostoGraduacao(usuarioDTO.getPostoGraduacao());
        usuarioModel.setNome(usuarioDTO.getNome());
        usuarioModel.setRe(usuarioDTO.getRe());

        return usuarioModel;
    }

    // Transformar um usuario do banco em dados para responder na API
    public UsuarioRespostaDTO map(UsuarioModel usuarioModel) {
        UsuarioRespostaDTO usuarioRespostaDTO = new UsuarioRespostaDTO();
        usuarioRespostaDTO.setId(usuarioModel.getId());
        usuarioRespostaDTO.setPostoGraduacao(usuarioModel.getPostoGraduacao());
        usuarioRespostaDTO.setNome(usuarioModel.getNome());
        usuarioRespostaDTO.setRe(usuarioModel.getRe());
        usuarioRespostaDTO.setAtivo(usuarioModel.isAtivo());

        return usuarioRespostaDTO;
    }
}
