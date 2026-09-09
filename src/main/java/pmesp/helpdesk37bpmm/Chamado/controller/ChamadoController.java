package pmesp.helpdesk37bpmm.Chamado.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoRespostaDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ResumoChamadosDTO;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.service.ChamadoService;

import java.util.List;

@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {

    private final ChamadoService chamadoService;

    @PostMapping("/cadastrar")
    public ChamadoRespostaDTO cadastrarChamado(@Valid @RequestBody ChamadoDTO chamadoDTO) {
        return chamadoService.criar(chamadoDTO);
    }

    @GetMapping("/buscar/{chamadoId}")
    public ChamadoRespostaDTO buscarChamadoPorId(@PathVariable Long chamadoId) {
        return chamadoService.buscarPorId(chamadoId);
    }

    @GetMapping("/meus")
    public List<ChamadoRespostaDTO> listarMeusChamados() {
        return chamadoService.listarChamadosDoUsuarioAutenticado();
    }

    @GetMapping("/fila")
    public List<ChamadoRespostaDTO> listarFilaAtendimento() {
        return chamadoService.listarFilaAtendimento();
    }

    @GetMapping("/em-atendimento")
    public List<ChamadoRespostaDTO> listarChamadosEmAtendimento() {
        return chamadoService.listarChamadosEmAtendimento();
    }

    @GetMapping("/resumo")
    public ResumoChamadosDTO obterResumoDeChamados() {
        return chamadoService.obterResumoDeChamados();
    }

    @PutMapping("/atualizar-dados/{chamadoId}")
    public ChamadoRespostaDTO atualizarPrioridadeDoChamado(@PathVariable Long chamadoId, @RequestParam ChamadoPrioridade prioridade) {
        return chamadoService.atualizarPrioridade(chamadoId, prioridade);
    }

    @PatchMapping("/iniciar-atendimento/{chamadoId}")
    public ChamadoRespostaDTO iniciarAtendimento(@PathVariable Long chamadoId, @RequestParam String reTecnico) {
        return chamadoService.iniciarAtendimento(chamadoId, reTecnico);
    }

    @PatchMapping("/transferir-responsavel/{chamadoId}")
    public ChamadoRespostaDTO transferirResponsavel(@PathVariable Long chamadoId, @RequestParam String reTecnico) {
        return chamadoService.transferirResponsavel(chamadoId, reTecnico);
    }

    @PatchMapping("/finalizar/{chamadoId}")
    public ChamadoRespostaDTO finalizarAtendimento(@PathVariable Long chamadoId, @RequestParam(required = false) String solucao) {
        return chamadoService.finalizarAtendimento(chamadoId, solucao);
    }

    @PatchMapping("/cancelar/{chamadoId}")
    public ChamadoRespostaDTO cancelarChamado(@PathVariable Long chamadoId, @RequestParam String motivoCancelamento) {
        return chamadoService.cancelarChamado(chamadoId, motivoCancelamento);
    }

}
