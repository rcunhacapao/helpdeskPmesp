package pmesp.helpdesk37bpmm.Seguranca;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.repository.TecnicoRepository;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

// Resolve o "problema do primeiro técnico": alguém precisa existir para poder cadastrar
// todo o resto. Roda uma vez ao subir a aplicação e só cria algo se ainda não existir
// nenhum técnico e as variáveis de ambiente de bootstrap estiverem preenchidas.
@Component
public class BootstrapTecnicoRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BootstrapTecnicoRunner.class);

    @Autowired
    private TecnicoRepository tecnicoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${bootstrap.tecnico.re:}")
    private String re;
    @Value("${bootstrap.tecnico.nome:}")
    private String nome;
    @Value("${bootstrap.tecnico.email:}")
    private String email;
    @Value("${bootstrap.tecnico.senha:}")
    private String senha;
    @Value("${bootstrap.tecnico.posto:SGT_3}")
    private String posto;

    @Override
    public void run(String... args) {
        if (tecnicoRepository.count() > 0) {
            return;
        }

        if (re.isBlank() || nome.isBlank() || email.isBlank() || senha.isBlank()) {
            log.warn("Nenhum técnico cadastrado ainda e as variáveis BOOTSTRAP_TECNICO_* não foram "
                    + "configuradas. Configure-as para criar o primeiro técnico automaticamente.");
            return;
        }

        UsuarioModel usuario = new UsuarioModel();
        usuario.setRe(re);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPostoGraduacao(UsuarioPostoGraduacao.valueOf(posto));
        usuario.setAtivo(true);
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuarioRepository.save(usuario);

        TecnicoModel tecnico = new TecnicoModel();
        tecnico.setUsuario(usuario);
        tecnico.setDisponivel(false);
        tecnicoRepository.save(tecnico);

        log.info("Primeiro técnico criado automaticamente (RE {}).", re);
    }
}
