package pmesp.helpdesk37bpmm.Autenticacao.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmesp.helpdesk37bpmm.Autenticacao.dto.LoginDTO;
import pmesp.helpdesk37bpmm.Autenticacao.dto.LoginRespostaDTO;
import pmesp.helpdesk37bpmm.Autenticacao.dto.TrocaSenhaDTO;
import pmesp.helpdesk37bpmm.Autenticacao.service.AutenticacaoService;
import pmesp.helpdesk37bpmm.Exception.AcessoNegadoException;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private SecurityContextRepository securityContextRepository;
    @Autowired
    private AutenticacaoService autenticacaoService;

    // Entrega ao frontend o token anti-CSRF da sessão sem expor nenhum dado de autenticação.
    @GetMapping("/csrf")
    public Map<String, String> consultarTokenCsrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    // Autentica com RE + senha e guarda o login na sessão para as próximas requisições
    @PostMapping("/login")
    public LoginRespostaDTO login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getRe(), loginDTO.getSenha()));

        salvarAutenticacaoNaSessao(autenticacao, request, response);

        return autenticacaoService.montarRespostaDeLogin(loginDTO.getRe());
    }

    // Permite ao frontend recuperar uma sessão que continua válida após atualizar a página.
    @GetMapping("/sessao")
    public LoginRespostaDTO consultarSessao() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();

        if (autenticacao == null || !autenticacao.isAuthenticated()
                || autenticacao instanceof AnonymousAuthenticationToken) {
            throw new AcessoNegadoException("SESSAO_NAO_AUTENTICADA", "Faça login para acessar o sistema.");
        }

        return autenticacaoService.montarRespostaDeLogin(autenticacao.getName());
    }

    // A sessão limitada criada pelo login RE/RE só pode concluir esta troca de senha.
    @PostMapping("/trocar-senha")
    public LoginRespostaDTO trocarSenha(@Valid @RequestBody TrocaSenhaDTO trocaSenhaDTO,
                                        Authentication autenticacao,
                                        HttpServletRequest request,
                                        HttpServletResponse response) {
        LoginRespostaDTO resposta = autenticacaoService.trocarSenhaObrigatoria(
                autenticacao.getName(), trocaSenhaDTO);

        // Atualiza a sessão para liberar as permissões normais imediatamente após a troca.
        Authentication autenticacaoAtualizada = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(autenticacao.getName(), trocaSenhaDTO.getNovaSenha()));
        salvarAutenticacaoNaSessao(autenticacaoAtualizada, request, response);
        return resposta;
    }

    private void salvarAutenticacaoNaSessao(Authentication autenticacao,
                                             HttpServletRequest request,
                                             HttpServletResponse response) {
        // Se já havia uma sessão anônima (por exemplo, criada para o token CSRF), troca o
        // identificador ao autenticar. Isso impede que um ID conhecido antes do login seja
        // reutilizado para sequestrar a sessão autenticada.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);
    }
}
