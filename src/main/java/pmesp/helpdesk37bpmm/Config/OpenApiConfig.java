package pmesp.helpdesk37bpmm.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Só o título/descrição que aparecem no topo do Swagger UI (/swagger-ui.html).
// A lista de endpoints é gerada automaticamente a partir dos controllers.
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApiDoHelpdesk() {
        return new OpenAPI().info(new Info()
                .title("Helpdesk Telemática - PMESP")
                .description("API do sistema de chamados da Telemática. Sessão por cookie: "
                        + "faça POST /auth/login antes de testar rotas protegidas nesta página.")
                .version("v0.0.1"));
    }
}
