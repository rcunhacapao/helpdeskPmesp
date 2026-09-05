package pmesp.helpdesk37bpmm.MikeIA.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.MikeIA.model.SolucaoConhecida;
import pmesp.helpdesk37bpmm.MikeIA.repository.SolucaoConhecidaRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MotorDeResolucaoBaseadoEmRegrasTest {

    @Mock
    SolucaoConhecidaRepository solucaoConhecidaRepository;

    @InjectMocks
    MotorDeResolucaoBaseadoEmRegras motor;

    @Test
    void deveUsarASolucaoDaCategoriaQuandoInformada() {
        SolucaoConhecida solucao = new SolucaoConhecida(1L, ChamadoCategoria.IMPRESSORA,
                "Impressora não imprime", "Verifique o papel.", "impressora", true, 1);
        when(solucaoConhecidaRepository.findByCategoriaAndAtivoTrueOrderByOrdemAsc(ChamadoCategoria.IMPRESSORA))
                .thenReturn(List.of(solucao));

        OrientacaoDoMike orientacao = motor.buscarSugestoes("minha impressora não funciona", ChamadoCategoria.IMPRESSORA);

        assertEquals("Verifique o papel.", orientacao.sugestoes());
        assertTrue(orientacao.possuiOrientacaoTestavel());
    }

    @Test
    void deveEncontrarSolucaoPelaPalavraChaveQuandoCategoriaNaoForInformada() {
        SolucaoConhecida solucao = new SolucaoConhecida(1L, ChamadoCategoria.REDE_INTERNET,
                "Sem internet", "Verifique o cabo de rede.", "internet, wifi", true, 1);
        when(solucaoConhecidaRepository.findByAtivoTrueOrderByOrdemAsc()).thenReturn(List.of(solucao));

        OrientacaoDoMike orientacao = motor.buscarSugestoes("estou sem internet no computador", null);

        assertEquals("Verifique o cabo de rede.", orientacao.sugestoes());
        assertTrue(orientacao.possuiOrientacaoTestavel());
    }

    @Test
    void deveDevolverMensagemPadraoQuandoNaoEncontrarNenhumaSolucao() {
        when(solucaoConhecidaRepository.findByAtivoTrueOrderByOrdemAsc()).thenReturn(List.of());

        OrientacaoDoMike orientacao = motor.buscarSugestoes("problema bem incomum e especifico", null);

        assertTrue(orientacao.sugestoes().toLowerCase().contains("chamado"));
        assertTrue(!orientacao.possuiOrientacaoTestavel());
    }
}
