package pmesp.helpdesk37bpmm.Autenticacao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Autenticacao.dto.LoginRespostaDTO;
import pmesp.helpdesk37bpmm.Autenticacao.dto.PrimeiroAcessoDTO;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Tecnico.repository.TecnicoRepository;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

@Service
public class AutenticacaoService {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Confirmar RE + e-mail do usuário pré-cadastrado e definir a senha dele
    public void realizarPrimeiroAcesso(PrimeiroAcessoDTO dto) {
        UsuarioModel usuario = validarIdentidadeParaPrimeiroAcesso(dto);
        definirSenha(usuario, dto.getNovaSenha());
    }

    // Etapa separada da definição de senha de propósito: se um dia entrar confirmação por
    // código de e-mail, essa validação de identidade é o lugar onde o código será conferido,
    // sem precisar mexer em como a senha é definida.
    private UsuarioModel validarIdentidadeParaPrimeiroAcesso(PrimeiroAcessoDTO dto) {
        UsuarioModel usuario = usuarioRepository.findByRe(dto.getRe())
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("USUARIO_INATIVO", "Usuário inativo não pode realizar o primeiro acesso.");
        }

        if (usuario.getSenhaHash() != null) {
            throw new RegraDeNegocioException("PRIMEIRO_ACESSO_JA_REALIZADO", "Este usuário já possui senha cadastrada.");
        }

        if (dto.getEmail() == null || !dto.getEmail().equalsIgnoreCase(usuario.getEmail())) {
            throw new RegraDeNegocioException("DADOS_PRIMEIRO_ACESSO_INVALIDOS",
                    "RE e e-mail não correspondem a um usuário cadastrado.");
        }

        return usuario;
    }

    private void definirSenha(UsuarioModel usuario, String novaSenha) {
        if (novaSenha == null || novaSenha.isBlank()) {
            throw new RegraDeNegocioException("SENHA_OBRIGATORIA", "Informe a nova senha.");
        }

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    // Montar os dados devolvidos depois de um login com sucesso
    public LoginRespostaDTO montarRespostaDeLogin(String re) {
        UsuarioModel usuario = usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));

        boolean tecnico = tecnicoRepository.findByUsuario(usuario).isPresent();
        return new LoginRespostaDTO(usuario.getIdentificacaoCompleta(), usuario.getRe(), tecnico);
    }
}
