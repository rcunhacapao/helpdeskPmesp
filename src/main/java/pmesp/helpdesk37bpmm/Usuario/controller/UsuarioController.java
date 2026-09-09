package pmesp.helpdesk37bpmm.Usuario.controller;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioAtualizacaoDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO;
import pmesp.helpdesk37bpmm.Usuario.service.UsuarioServices;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioServices usuarioServices;

    @PostMapping("/cadastrar")
    public UsuarioRespostaDTO cadastrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioServices.criar(usuarioDTO);
    }


    @GetMapping("/buscar/{re}")
    public UsuarioRespostaDTO buscarUsuarioPorRe(@PathVariable String re) {
        return usuarioServices.buscarPorRe(re);
    }


    @PutMapping("/atualizar-dados/{re}")
    public UsuarioRespostaDTO atualizarUsuario(@PathVariable String re, @Valid @RequestBody UsuarioAtualizacaoDTO dadosAtualizados) {
        return usuarioServices.atualizarUsuario(re, dadosAtualizados);
    }


    @PatchMapping("/inativar/{re}")
    public boolean inativarUsuario(@PathVariable String re) {
         return usuarioServices.inativarPolicialPorRe(re);
    }


    @PatchMapping("/resetar-senha/{re}")
    public UsuarioRespostaDTO resetarSenha(@PathVariable String re) {
        return usuarioServices.resetarSenha(re);
    }

}
