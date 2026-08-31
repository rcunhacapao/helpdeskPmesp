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

    // Cadastrar novo usuario
    public UsuarioModel criar(UsuarioDTO usuarioDTO) {
        // Aceitar apenas o RE sem o digito
        if (usuarioDTO == null || usuarioDTO.getRe() == null || !usuarioDTO.getRe().matches("[0-9]{1,6}")) {
            return null;
        }

        if (usuarioRespository.findByRe(usuarioDTO.getRe()).isPresent()) {
            return null;
        }

        UsuarioModel usuarioNovo = new UsuarioModel();
        usuarioNovo.setPostoGraduacao(usuarioDTO.getPostoGraduacao());
        usuarioNovo.setNome(usuarioDTO.getNome());
        usuarioNovo.setRe(usuarioDTO.getRe());
        usuarioNovo.setAtivo(true);

        return usuarioRespository.save(usuarioNovo);
    }


    // Pesquisar usuario por RE
    public UsuarioModel buscarPorRe(String re)  {
        Optional<UsuarioModel> buscarRe = usuarioRespository.findByRe(re);
        return buscarRe.orElse(null);
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


    // Atualizar dados do usuario
    public UsuarioModel atualizarUsuario(String re, UsuarioModel dadosAtualizados) {
        Optional<UsuarioModel> usuarioAtual = usuarioRespository.findByRe(re);
        if (usuarioAtual.isPresent()) {
            UsuarioModel usuario = usuarioAtual.get();
            usuario.setNome(dadosAtualizados.getNome());
            usuario.setPostoGraduacao(dadosAtualizados.getPostoGraduacao());

            return usuarioRespository.save(usuario);
        }
        return null;
    }

}
