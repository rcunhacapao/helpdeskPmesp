package pmesp.helpdesk37bpmm.Autenticacao.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Autenticacao.dto.LoginRespostaDTO;
import pmesp.helpdesk37bpmm.Autenticacao.dto.TrocaSenhaDTO;
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

    // Concluir a troca exigida depois do primeiro login ou de um reset técnico.
    public LoginRespostaDTO trocarSenhaObrigatoria(String re, TrocaSenhaDTO dto) {
        UsuarioModel usuario = usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("USUARIO_INATIVO", "Usuário inativo não pode alterar a senha.");
        }

        if (!usuario.isTrocaSenhaObrigatoria()) {
            throw new RegraDeNegocioException("TROCA_SENHA_NAO_PENDENTE",
                    "Não existe uma troca obrigatória de senha pendente para este usuário.");
        }

        if (!dto.getNovaSenha().equals(dto.getConfirmacaoNovaSenha())) {
            throw new RegraDeNegocioException("SENHAS_NAO_CONFEREM", "As senhas informadas não são iguais.");
        }

        if (dto.getNovaSenha().length() < 6) {
            throw new RegraDeNegocioException("SENHA_MUITO_CURTA", "A nova senha deve ter pelo menos 6 caracteres.");
        }

        if (dto.getNovaSenha().equals(usuario.getRe())) {
            throw new RegraDeNegocioException("SENHA_IGUAL_AO_RE", "Crie uma senha diferente do seu RE.");
        }

        usuario.setSenhaHash(passwordEncoder.encode(dto.getNovaSenha()));
        usuario.setTrocaSenhaObrigatoria(false);
        usuarioRepository.save(usuario);
        return montarRespostaDeLogin(re);
    }

    // Montar os dados devolvidos depois de um login com sucesso
    public LoginRespostaDTO montarRespostaDeLogin(String re) {
        UsuarioModel usuario = usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));

        boolean tecnico = tecnicoRepository.findByUsuario(usuario).isPresent();
        return new LoginRespostaDTO(usuario.getIdentificacaoCompleta(), usuario.getRe(), tecnico,
                usuario.isTrocaSenhaObrigatoria());
    }
}
