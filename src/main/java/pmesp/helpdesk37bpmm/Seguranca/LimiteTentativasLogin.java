package pmesp.helpdesk37bpmm.Seguranca;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Exception.MuitasTentativasException;

import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// Limita tentativas repetidas sem bloquear a conta globalmente. A chave combina a origem
// da conexão com o RE, reduzindo força bruta e evitando que uma pessoa bloqueie outra.
@Service
public class LimiteTentativasLogin {

    private static final int TENTATIVAS_POR_MINUTO = 5;

    // O cache expira sozinho e tem teto para não crescer indefinidamente com entradas falsas.
    private final Cache<String, Bucket> limites = Caffeine.newBuilder()
            .expireAfterAccess(15, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public void consumirTentativa(String enderecoRemoto, String re) {
        String chave = criarChave(enderecoRemoto, re);
        Bucket limite = limites.get(chave, ignorada -> criarLimite());
        if (limite == null || !limite.tryConsume(1)) {
            throw new MuitasTentativasException();
        }
    }

    public void registrarSucesso(String enderecoRemoto, String re) {
        limites.invalidate(criarChave(enderecoRemoto, re));
    }

    private Bucket criarLimite() {
        Bandwidth faixa = Bandwidth.builder()
                .capacity(TENTATIVAS_POR_MINUTO)
                .refillIntervally(TENTATIVAS_POR_MINUTO, Duration.ofMinutes(1))
                .build();
        return Bucket.builder().addLimit(faixa).build();
    }

    private String criarChave(String enderecoRemoto, String re) {
        String origem = enderecoRemoto == null || enderecoRemoto.isBlank() ? "origem-desconhecida" : enderecoRemoto;
        String conta = re != null && re.matches("[0-9]{1,6}")
                ? re
                : "re-invalido";
        return origem.toLowerCase(Locale.ROOT) + ':' + conta;
    }
}
