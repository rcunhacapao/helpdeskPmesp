package pmesp.helpdesk37bpmm.RelatoErro.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatarErroDTO;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatoErroRespostaDTO;
import pmesp.helpdesk37bpmm.RelatoErro.service.RelatoErroService;

import java.util.List;

@RestController
@RequestMapping("/relatos-erro")
@RequiredArgsConstructor
public class RelatoErroController {

    private final RelatoErroService relatoErroService;

    @PostMapping
    public RelatoErroRespostaDTO relatar(@Valid @RequestBody RelatarErroDTO relatarErroDTO) {
        return relatoErroService.relatar(relatarErroDTO);
    }

    @GetMapping
    public List<RelatoErroRespostaDTO> listarTodos() {
        return relatoErroService.listarTodos();
    }
}
