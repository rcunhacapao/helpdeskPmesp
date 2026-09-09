package pmesp.helpdesk37bpmm.Tecnico.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoDTO;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoRespostaDTO;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;

import java.util.List;

@RestController
@RequestMapping("/tecnicos")
@RequiredArgsConstructor
public class TecnicoController {

    private final TecnicoService tecnicoService;

    @PostMapping("/cadastrar")
    public TecnicoRespostaDTO cadastrarTecnico(@Valid @RequestBody TecnicoDTO tecnicoDTO) {
        return tecnicoService.criar(tecnicoDTO);
    }

    @GetMapping("/buscar/{re}")
    public TecnicoRespostaDTO buscarTecnicoPorRe(@PathVariable String re) {
        return tecnicoService.buscarPorRe(re);
    }

    @GetMapping
    public List<TecnicoRespostaDTO> listarTodosOsTecnicos() {
        return tecnicoService.listarTodosOsTecnicos();
    }

    @GetMapping("/disponiveis")
    public List<TecnicoRespostaDTO> listarTecnicosDisponiveis() {
        return tecnicoService.listarTecnicosDisponiveis();
    }

    @PatchMapping("/ficar-disponivel/{re}")
    public TecnicoRespostaDTO ficarDisponivel(@PathVariable String re) {
        return tecnicoService.ficarDisponivel(re);
    }

    @PatchMapping("/ficar-indisponivel/{re}")
    public TecnicoRespostaDTO ficarIndisponivel(@PathVariable String re) {
        return tecnicoService.ficarIndisponivel(re);
    }
}
