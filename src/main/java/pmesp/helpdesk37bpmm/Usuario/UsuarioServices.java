package pmesp.helpdesk37bpmm.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Service
public class UsuarioServices {

    @Autowired
    UsuarioRespository usuarioRespository;

    // Cadastrar novo policial
    public UsuarioModel criar(@RequestBody UsuarioModel usuarioNovo) {
        return usuarioRespository.save(usuarioNovo);
    }

    // Pesquisar Policial por RE
    public UsuarioModel buscarPorRe(Long re)  {
        Optional<UsuarioModel> buscarRe = usuarioRespository.findByRe(re);
        return buscarRe.orElse(null);
    }

    // Inativar Policial por RE (Transferencia de BTL)
    public boolean inativarPolicialPorRe(Long re) {
        Optional<UsuarioModel> policial = usuarioRespository.findByRe(re);
        if (policial.isPresent()) {
            UsuarioModel usuario = policial.get();
            usuario.setAtivo(false);
            usuarioRespository.save(usuario);
            return true;
        }
        return false;
    }

    // Atualizar dados do Policial
    public UsuarioModel atualizarUsuario(Long re, UsuarioModel dadosAtualizados) {
        Optional<UsuarioModel> usuarioAtual = usuarioRespository.findByRe(re);
        if (usuarioAtual.isPresent()) {
            UsuarioModel usuario = usuarioAtual.get();
            usuario.setNome(dadosAtualizados.getNome());
            usuario.setPostoGraduacao(dadosAtualizados.getPostoGraduacao());

            return usuarioRespository.save(usuario);
        }
        return null;
    }

    // Apagar policial do banco de dados
       public void deletarPolicial(Long re) {
           usuarioRespository.deleteByRe(re);
       }
}
