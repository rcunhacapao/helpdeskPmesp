package pmesp.helpdesk37bpmm.MikeIA.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.MikeIA.dto.AtendimentoMikeIARespostaDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.ConcluirAtendimentoMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.EncaminharChamadoDoMikeDTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.IniciarAtendimentoMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.dto.MetricasMikeIADTO;
import pmesp.helpdesk37bpmm.MikeIA.service.MikeIAService;

@RestController
@RequestMapping("/mike-ia")
@RequiredArgsConstructor
public class MikeIAController {

    private final MikeIAService mikeIAService;

    @PostMapping("/iniciar")
    public AtendimentoMikeIARespostaDTO iniciar(@Valid @RequestBody IniciarAtendimentoMikeIADTO dto) {
        return mikeIAService.iniciar(dto);
    }

    @GetMapping("/em-diagnostico")
    public ResponseEntity<AtendimentoMikeIARespostaDTO> buscarDiagnosticoEmAndamento() {
        return mikeIAService.buscarDiagnosticoEmAndamento()
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

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

    @PatchMapping("/concluir/{atendimentoId}")
    public AtendimentoMikeIARespostaDTO concluirComoResolvido(@PathVariable Long atendimentoId, @Valid @RequestBody ConcluirAtendimentoMikeIADTO dto) {
        return mikeIAService.concluirComoResolvido(atendimentoId, dto.getResumoAtendimento());
    }

    @PatchMapping("/encaminhar/{atendimentoId}")
    public AtendimentoMikeIARespostaDTO encaminharParaEquipeTecnica(@PathVariable Long atendimentoId, @Valid @RequestBody EncaminharChamadoDoMikeDTO dto) {
        return mikeIAService.encaminharParaEquipeTecnica(atendimentoId, dto);
    }

    @PatchMapping("/abandonar/{atendimentoId}")
    public ResponseEntity<Void> abandonarDiagnostico(@PathVariable Long atendimentoId) {
        mikeIAService.abandonarDiagnostico(atendimentoId);
        return ResponseEntity.noContent().build();
    }
}
