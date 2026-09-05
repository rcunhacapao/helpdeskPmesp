package pmesp.helpdesk37bpmm.MikeIA.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.MikeIA.model.SolucaoConhecida;
import pmesp.helpdesk37bpmm.MikeIA.repository.SolucaoConhecidaRepository;

import java.util.List;
import java.util.Optional;

// Primeira versão do Mike IA: nada de inteligência artificial de verdade, só busca na
// base de soluções conhecidas (SolucaoConhecida). Quando a categoria é informada, usa
// ela direto; quando não, tenta achar uma solução cujas palavras-chave apareçam no
// texto que o usuário escreveu.
@Service
public class MotorDeResolucaoBaseadoEmRegras implements MotorDeResolucaoMikeIA {

    private static final String SEM_SUGESTAO = "Ainda não tenho uma sugestão pronta para esse tipo de "
            + "problema. Você pode prosseguir para abrir um chamado para a equipe técnica.";

    @Autowired
    private SolucaoConhecidaRepository solucaoConhecidaRepository;

    @Override
    public OrientacaoDoMike buscarSugestoes(String descricaoProblema, ChamadoCategoria categoria) {
        Optional<SolucaoConhecida> solucao = categoria != null
                ? primeiraSolucaoDaCategoria(categoria)
                : encontrarPelasPalavrasChave(descricaoProblema);

        return solucao
                .map(solucaoConhecida -> new OrientacaoDoMike(solucaoConhecida.getPassos(), true))
                .orElseGet(() -> new OrientacaoDoMike(SEM_SUGESTAO, false));
    }

    private Optional<SolucaoConhecida> primeiraSolucaoDaCategoria(ChamadoCategoria categoria) {
        List<SolucaoConhecida> solucoes = solucaoConhecidaRepository.findByCategoriaAndAtivoTrueOrderByOrdemAsc(categoria);
        return solucoes.isEmpty() ? Optional.empty() : Optional.of(solucoes.get(0));
    }

    private Optional<SolucaoConhecida> encontrarPelasPalavrasChave(String descricaoProblema) {
        String textoDigitado = descricaoProblema.toLowerCase();

        for (SolucaoConhecida solucao : solucaoConhecidaRepository.findByAtivoTrueOrderByOrdemAsc()) {
            if (solucao.getPalavrasChave() == null) {
                continue;
            }
            for (String palavraChave : solucao.getPalavrasChave().split(",")) {
                if (textoDigitado.contains(palavraChave.trim().toLowerCase())) {
                    return Optional.of(solucao);
                }
            }
        }

        return Optional.empty();
    }
}
