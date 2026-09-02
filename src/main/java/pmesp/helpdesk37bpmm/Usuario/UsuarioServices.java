package pmesp.helpdesk37bpmm.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Chamado.ChamadoService;

import java.util.Optional;

@Service
public class UsuarioServices {

    @Autowired
    UsuarioRepository usuarioRespository;

    @Autowired
    ChamadoService chamadoService;

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
            usuario.setAtivo(false);
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
