package pmesp.helpdesk37bpmm.Chamado.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoDTO;
import pmesp.helpdesk37bpmm.Chamado.dto.ChamadoRespostaDTO;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.service.ChamadoService;

import java.util.List;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    // Cadastrar novo chamado
    @PostMapping("/cadastrar")
    public ChamadoRespostaDTO cadastrarChamado(@RequestBody ChamadoDTO chamadoDTO) {
        return chamadoService.criar(chamadoDTO);
    }


    // Buscar chamado por ID
    @GetMapping("/buscar/{chamadoId}")
    public ChamadoRespostaDTO buscarChamadoPorId(@PathVariable Long chamadoId) {
        return chamadoService.buscarPorId(chamadoId);
    }


    // Mostrar fila de chamados que aguardam atendimento
    @GetMapping("/fila")
    public List<ChamadoRespostaDTO> listarFilaAtendimento() {
        return chamadoService.listarFilaAtendimento();
    }


    // Atualizar prioridade do chamado
    @PutMapping("/atualizar-dados/{chamadoId}")
    public ChamadoRespostaDTO atualizarDadosChamado(@PathVariable Long chamadoId, @RequestParam ChamadoPrioridade prioridade) {
        return chamadoService.atualizarPrioridade(chamadoId, prioridade);
    }


    // Iniciar atendimento do chamado
    @PatchMapping("/iniciar-atendimento/{chamadoId}")
    public ChamadoRespostaDTO iniciarAtendimento(@PathVariable Long chamadoId) {
        return chamadoService.iniciarAtendimento(chamadoId);
    }


    // Finalizar atendimento do chamado
    @PatchMapping("/finalizar/{chamadoId}")
    public ChamadoRespostaDTO finalizarAtendimento(@PathVariable Long chamadoId) {
        return chamadoService.finalizarAtendimento(chamadoId);
    }


    // Cancelar Chamado
    @PatchMapping("/cancelar/{chamadoId}")
    public ChamadoRespostaDTO cancelarChamado(@PathVariable Long chamadoId, @RequestParam String motivoCancelamento) {
        return chamadoService.cancelarChamado(chamadoId, motivoCancelamento);
    }

}
