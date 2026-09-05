package pmesp.helpdesk37bpmm.Seguranca;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Tecnico.repository.TecnicoRepository;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;

// Ensina o Spring Security a carregar um usuário do nosso banco. O "username" usado em
// todo o login é sempre o RE (sem dígito), do mesmo jeito que o resto do sistema já usa.
@Service
public class UsuarioDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Override
    public UserDetails loadUserByUsername(String re) throws UsernameNotFoundException {
        UsuarioModel usuario = usuarioRepository.findByRe(re)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        // Sem senha ainda = não completou o primeiro acesso, então não pode logar
        boolean aindaNaoFezPrimeiroAcesso = usuario.getSenhaHash() == null;

        return User.withUsername(usuario.getRe())
                .password(aindaNaoFezPrimeiroAcesso ? "" : usuario.getSenhaHash())
                .authorities(construirAutoridades(usuario))
                .disabled(!usuario.isAtivo() || aindaNaoFezPrimeiroAcesso)
                .build();
    }

    // Todo usuário tem ROLE_USUARIO; quem também tem um cadastro de técnico ganha ROLE_TECNICO.
    // Calculado aqui (não salvo em coluna) para facilitar adicionar um novo perfil no futuro.
    private List<GrantedAuthority> construirAutoridades(UsuarioModel usuario) {
        List<GrantedAuthority> autoridades = new ArrayList<>();
        autoridades.add(new SimpleGrantedAuthority("ROLE_USUARIO"));

        if (tecnicoRepository.findByUsuario(usuario).isPresent()) {
            autoridades.add(new SimpleGrantedAuthority("ROLE_TECNICO"));
        }

        return autoridades;
    }
}
