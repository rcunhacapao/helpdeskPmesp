package pmesp.helpdesk37bpmm.Usuario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Optional;

@Service
public class UsuarioServices {

    @Autowired
    UsuarioRepository usuarioRespository;

    // Cadastrar novo usuario
    public UsuarioModel criar(UsuarioModel usuarioNovo) {
        if (usuarioNovo.getRe() != null && usuarioNovo.getRe().length() > 6) {
            throw new IllegalArgumentException("Erro: Por favor, insira o RE sem o dígito e sem traços.");
        }

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

    // Apagar usuario do banco de dados
       public void deletarPolicial(String re) {
           usuarioRespository.deleteByRe(re);
       }
}
