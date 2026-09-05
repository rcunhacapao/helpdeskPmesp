package pmesp.helpdesk37bpmm.RelatoErro.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatarErroDTO;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatoErroRespostaDTO;
import pmesp.helpdesk37bpmm.RelatoErro.service.RelatoErroService;

import java.util.List;

@RestController
@RequestMapping("/relatos-erro")
public class RelatoErroController {

    @Autowired
    private RelatoErroService relatoErroService;

    // Registrar um erro encontrado no sistema (qualquer pessoa logada)
    @PostMapping
    public RelatoErroRespostaDTO relatar(@Valid @RequestBody RelatarErroDTO relatarErroDTO) {
        return relatoErroService.relatar(relatarErroDTO);
    }

    // Consultar todos os relatos enviados (somente técnico)
    @GetMapping
    public List<RelatoErroRespostaDTO> listarTodos() {
        return relatoErroService.listarTodos();
    }
}
