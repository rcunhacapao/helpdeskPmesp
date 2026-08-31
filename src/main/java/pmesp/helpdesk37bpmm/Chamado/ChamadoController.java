package pmesp.helpdesk37bpmm.Chamado;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chamados")
public class ChamadoController {

    @Autowired
    private ChamadoService chamadoService;

    // Cadastrar novo chamado
    @PostMapping("/cadastrar")
    public ChamadoModel cadastrarChamado(@RequestBody ChamadoDTO chamadoDTO) {
        return chamadoService.criar(chamadoDTO);
    }


    // Buscar chamado por ID
    @GetMapping("/buscar/{chamadoId}")
    public ChamadoModel buscarChamadoPorId(@PathVariable Long chamadoId) {
        return chamadoService.buscarPorId(chamadoId);
    }


    // Atualizar prioridade do chamado
    @PutMapping("/atualizar-dados/{chamadoId}")
    public ChamadoModel atualizarDadosChamado(@PathVariable Long chamadoId, @RequestParam ChamadoPrioridade prioridade) {
        return chamadoService.atualizarPrioridade(chamadoId, prioridade);
    }


    // Iniciar atendimento do chamado
    @PatchMapping("/iniciar-atendimento/{chamadoId}")
    public ChamadoModel iniciarAtendimento(@PathVariable Long chamadoId) {
        return chamadoService.iniciarAtendimento(chamadoId);
    }


    // Finalizar atendimento do chamado
    @PatchMapping("/finalizar/{chamadoId}")
    public ChamadoModel finalizarAtendimento(@PathVariable Long chamadoId) {
        return chamadoService.finalizarAtendimento(chamadoId);
    }


    // Cancelar Chamado
    @PatchMapping("/cancelar/{chamadoId}")
    public ChamadoModel cancelarChamado(@PathVariable Long chamadoId, @RequestParam String motivoCancelamento) {
        return chamadoService.cancelarChamado(chamadoId, motivoCancelamento);
    }

}
