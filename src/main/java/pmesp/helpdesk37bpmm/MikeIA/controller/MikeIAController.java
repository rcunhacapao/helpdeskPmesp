package pmesp.helpdesk37bpmm.MikeIA.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.MikeIA.dto.AtendimentoMikeIARespostaDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.EncaminharChamadoDoMikeDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.IniciarAtendimentoMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.MetricasMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.service.MikeIAService;

@RestController
@RequestMapping("/mike-ia")
public class MikeIAController {

    @Autowired
    private MikeIAService mikeIAService;

    // O primeiro relato cria apenas o diagnóstico temporário do Mike.
    @PostMapping("/iniciar")
    public AtendimentoMikeIARespostaDTO iniciar(@Valid @RequestBody IniciarAtendimentoMikeIADTO dto) {
        return mikeIAService.iniciar(dto);
    }

    // Permite retomar o mesmo atendimento depois de recarregar a página.
    @GetMapping("/em-diagnostico")
    public ResponseEntity<AtendimentoMikeIARespostaDTO> buscarDiagnosticoEmAndamento() {
        return mikeIAService.buscarDiagnosticoEmAndamento()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    // Contexto do diagnóstico exibido apenas no detalhe da fila técnica.
    @GetMapping("/chamado/{chamadoId}")
    public ResponseEntity<AtendimentoMikeIARespostaDTO> buscarHistoricoDoChamado(@PathVariable Long chamadoId) {
        return mikeIAService.buscarHistoricoDoChamadoParaTecnico(chamadoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/metricas")
    public MetricasMikeIADTO obterMetricas() {
        return mikeIAService.obterMetricasParaTecnico();
    }

    // O usuário confirmou que as orientações resolveram o problema.
    @PatchMapping("/concluir/{atendimentoId}")
    public AtendimentoMikeIARespostaDTO concluirComoResolvido(@PathVariable Long atendimentoId) {
        return mikeIAService.concluirComoResolvido(atendimentoId);
    }

    // O Mike não resolveu; o formulário enviado cria o chamado para a fila técnica.
    @PatchMapping("/encaminhar/{atendimentoId}")
    public AtendimentoMikeIARespostaDTO encaminharParaEquipeTecnica(@PathVariable Long atendimentoId,
                                                                      @Valid @RequestBody EncaminharChamadoDoMikeDTO dto) {
        return mikeIAService.encaminharParaEquipeTecnica(atendimentoId, dto);
    }

    // O usuário desistiu do diagnóstico antes de gerar um chamado.
    @PatchMapping("/abandonar/{atendimentoId}")
    public ResponseEntity<Void> abandonarDiagnostico(@PathVariable Long atendimentoId) {
        mikeIAService.abandonarDiagnostico(atendimentoId);
        return ResponseEntity.noContent().build();
    }
}
