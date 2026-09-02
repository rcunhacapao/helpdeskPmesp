package pmesp.helpdesk37bpmm.Usuario.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Chamado.service.ChamadoService;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioAtualizacaoDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO;
import pmesp.helpdesk37bpmm.Usuario.mapper.UsuarioMapper;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioServices {

    @Autowired
    UsuarioRepository usuarioRespository;

    @Autowired
    ChamadoService chamadoService;

    @Autowired
    TecnicoService tecnicoService;

    @Autowired
    UsuarioMapper usuarioMapper;

    // Cadastrar novo usuario
    public UsuarioRespostaDTO criar(UsuarioDTO usuarioDTO) {
        // Aceitar apenas o RE sem o digito
        if (usuarioDTO == null || usuarioDTO.getRe() == null || !usuarioDTO.getRe().matches("[0-9]{1,6}")) {

            return null;
        }

        if (usuarioRespository.findByRe(usuarioDTO.getRe()).isPresent()) {
            return null;
        }


        // Transformar os dados do cadastro em usuario para salvar no banco
        UsuarioModel usuarioNovo = usuarioMapper.map(usuarioDTO);

        // Todo novo usuario começa ativo
        usuarioNovo.setAtivo(true);

        // Transformar o usuario salvo em resposta para a API
        return usuarioMapper.map(usuarioRespository.save(usuarioNovo));
    }


    // Pesquisar usuario por RE
    public UsuarioRespostaDTO buscarPorRe(String re)  {
        Optional<UsuarioModel> buscarRe = usuarioRespository.findByRe(re);
        if (buscarRe.isPresent()) {
            return usuarioMapper.map(buscarRe.get());
        }
        return null;
    }


    // Inativar usuario por RE (Transferencia de BTL)
    public boolean inativarPolicialPorRe(String re) {
        Optional<UsuarioModel> policial = usuarioRespository.findByRe(re);
        if (policial.isPresent()) {
            UsuarioModel usuario = policial.get();
            Optional<TecnicoModel> tecnico = tecnicoService.buscarPorUsuario(usuario);

            // Não inativar técnico que ainda tem chamados para atender
            if (tecnico.isPresent() && chamadoService.temChamadosPendentesDoTecnico(tecnico.get())) {
                return false;
            }

            usuario.setAtivo(false);
            if (tecnico.isPresent()) {
                tecnicoService.tornarIndisponivel(tecnico.get());
            }
            chamadoService.cancelarChamadosAbertosDoUsuario(usuario);
            usuarioRespository.save(usuario);
            return true;
        }
        return false;
    }


    // Atualizar apenas nome e posto/graduação do usuario
    public UsuarioRespostaDTO atualizarUsuario(String re, UsuarioAtualizacaoDTO dadosAtualizados) {
        Optional<UsuarioModel> usuarioAtual = usuarioRespository.findByRe(re);
        if (usuarioAtual.isPresent()) {
            UsuarioModel usuario = usuarioAtual.get();
            usuario.setNome(dadosAtualizados.getNome());
            usuario.setPostoGraduacao(dadosAtualizados.getPostoGraduacao());

            // Transformar o usuario atualizado em resposta para a API
            return usuarioMapper.map(usuarioRespository.save(usuario));
        }
        return null;
    }

}
