package pmesp.helpdesk37bpmm.Seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Testa o login de ponta a ponta através do AuthenticationManager real do Spring Security
// (o mesmo usado pelo AutenticacaoController): confirma que o técnico criado pelo
// BootstrapTecnicoRunner consegue logar com a senha correta, recebe o perfil de técnico,
// e é recusado com a senha errada.
@SpringBootTest
@TestPropertySource(properties = {
        "DATABASE_URL=jdbc:h2:mem:helpdesk_seguranca_test;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD=",
        "bootstrap.tecnico.re=100001",
        "bootstrap.tecnico.nome=Tecnico Bootstrap",
        "bootstrap.tecnico.email=tecnico.bootstrap@policiamilitar.sp.gov.br",
        "bootstrap.tecnico.senha=senha123"
})
class SegurancaIntegrationTest {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Test
    void deveAutenticarTecnicoDeBootstrapComASenhaCorretaEReceberOPerfilDeTecnico() {
        Authentication autenticacao = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("100001", "senha123"));

        assertTrue(autenticacao.isAuthenticated());
        assertTrue(autenticacao.getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_TECNICO")));
    }

    @Test
    void deveRecusarLoginComSenhaErrada() {
        assertThrows(BadCredentialsException.class, () -> authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("100001", "senha-errada")));
    }
}
