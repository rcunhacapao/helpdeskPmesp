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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pmesp.helpdesk37bpmm.Autenticacao.dto.LoginDTO;
import pmesp.helpdesk37bpmm.Autenticacao.dto.LoginRespostaDTO;
import pmesp.helpdesk37bpmm.Autenticacao.dto.PrimeiroAcessoDTO;
import pmesp.helpdesk37bpmm.Autenticacao.service.AutenticacaoService;
import pmesp.helpdesk37bpmm.Exception.AcessoNegadoException;

@RestController
@RequestMapping("/auth")
public class AutenticacaoController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private SecurityContextRepository securityContextRepository;
    @Autowired
    private AutenticacaoService autenticacaoService;

    // Autentica com RE + senha e guarda o login na sessão para as próximas requisições
    @PostMapping("/login")
    public LoginRespostaDTO login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request, HttpServletResponse response) {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getRe(), loginDTO.getSenha()));

        // O AuthenticationManager só confere a senha; para o login "grudar" nas próximas
        // requisições, é preciso guardar o resultado explicitamente na sessão.
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        securityContextRepository.saveContext(contexto, request, response);

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

    // Primeiro acesso: confirma RE + e-mail funcional e define a senha do usuário pré-cadastrado
    @PostMapping("/primeiro-acesso")
    public void primeiroAcesso(@Valid @RequestBody PrimeiroAcessoDTO primeiroAcessoDTO) {
        autenticacaoService.realizarPrimeiroAcesso(primeiroAcessoDTO);
    }
}
