package pmesp.helpdesk37bpmm.Tecnico.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoDTO;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoRespostaDTO;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;

import java.util.List;

@RestController
@RequestMapping("/tecnicos")
public class TecnicoController {

    @Autowired
    private TecnicoService tecnicoService;

    // Cadastrar usuário existente como técnico
    @PostMapping("/cadastrar")
    public TecnicoRespostaDTO cadastrarTecnico(@Valid @RequestBody TecnicoDTO tecnicoDTO) {
        return tecnicoService.criar(tecnicoDTO);
    }


    // Buscar técnico pelo RE do usuário relacionado
    @GetMapping("/buscar/{re}")
    public TecnicoRespostaDTO buscarTecnicoPorRe(@PathVariable String re) {
        return tecnicoService.buscarPorRe(re);
    }


    // Listar todos os técnicos cadastrados (disponíveis ou não)
    @GetMapping
    public List<TecnicoRespostaDTO> listarTodosOsTecnicos() {
        return tecnicoService.listarTodosOsTecnicos();
    }


    // Mostrar técnicos disponíveis para atendimento neste momento
    @GetMapping("/disponiveis")
    public List<TecnicoRespostaDTO> listarTecnicosDisponiveis() {
        return tecnicoService.listarTecnicosDisponiveis();
    }


    // Informar que o técnico pode receber chamados
    @PatchMapping("/ficar-disponivel/{re}")
    public TecnicoRespostaDTO ficarDisponivel(@PathVariable String re) {
        return tecnicoService.ficarDisponivel(re);
    }


    // Informar que o técnico não pode receber novos chamados
    @PatchMapping("/ficar-indisponivel/{re}")
    public TecnicoRespostaDTO ficarIndisponivel(@PathVariable String re) {
        return tecnicoService.ficarIndisponivel(re);
    }
}
