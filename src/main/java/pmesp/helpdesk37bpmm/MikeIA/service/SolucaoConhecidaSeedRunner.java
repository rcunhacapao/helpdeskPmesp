package pmesp.helpdesk37bpmm.MikeIA.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.MikeIA.model.SolucaoConhecida;
import pmesp.helpdesk37bpmm.MikeIA.repository.SolucaoConhecidaRepository;

// Cadastra algumas soluções de exemplo na primeira vez que a aplicação sobe, só para o
// Mike IA ter o que sugerir. Não existe ainda uma tela para cadastrar soluções — quando
// essa tela existir, este seed pode ser removido.
@Component
public class SolucaoConhecidaSeedRunner implements CommandLineRunner {

    @Autowired
    private SolucaoConhecidaRepository solucaoConhecidaRepository;

    @Override
    public void run(String... args) {
        if (solucaoConhecidaRepository.count() > 0) {
            return;
        }

        solucaoConhecidaRepository.save(new SolucaoConhecida(null, ChamadoCategoria.IMPRESSORA,
                "Impressora não imprime",
                "Verifique se a impressora está ligada e conectada ao computador.\n"
                        + "Confira se há papel na bandeja.\n"
                        + "Veja se a impressora correta está selecionada no computador.\n"
                        + "Desligue a impressora, espere alguns segundos e ligue de novo.\n"
                        + "Tente imprimir outra vez.",
                "impressora, imprimir, papel",
                true, 1));

        solucaoConhecidaRepository.save(new SolucaoConhecida(null, ChamadoCategoria.COMPUTADOR,
                "Computador não liga",
                "Confira se o cabo de energia está bem conectado na tomada e no computador.\n"
                        + "Tente trocar de tomada.\n"
                        + "Verifique se alguma luz ou som indica que o computador está recebendo energia.",
                "computador, nao liga, ligar, energia",
                true, 1));

        solucaoConhecidaRepository.save(new SolucaoConhecida(null, ChamadoCategoria.REDE_INTERNET,
                "Sem acesso à internet ou rede",
                "Verifique se o cabo de rede está bem conectado.\n"
                        + "Confira se o Wi-Fi está ativado, se for o caso.\n"
                        + "Tente reiniciar o computador.\n"
                        + "Veja se outros computadores do mesmo local também estão sem acesso.",
                "internet, rede, wifi, conexao",
                true, 1));
    }
}
