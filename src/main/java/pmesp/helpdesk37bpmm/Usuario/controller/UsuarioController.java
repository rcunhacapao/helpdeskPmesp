package pmesp.helpdesk37bpmm.Usuario.controller;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioAtualizacaoDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO;
import pmesp.helpdesk37bpmm.Usuario.service.UsuarioServices;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioServices usuarioServices;

    // Cadastrar usuario
    @PostMapping("/cadastrar")
    public UsuarioRespostaDTO cadastrarUsuario(@Valid @RequestBody UsuarioDTO usuarioDTO) {
        return usuarioServices.criar(usuarioDTO);
    }


    // Buscar por RE
    @GetMapping("/buscar/{re}")
    public UsuarioRespostaDTO buscarUsuarioPorRe(@PathVariable String re) {
        return usuarioServices.buscarPorRe(re);
    }


    // Atualizar dados
    @PutMapping("/atualizar-dados/{re}")
    public UsuarioRespostaDTO atualizarUsuario(@PathVariable String re, @Valid @RequestBody UsuarioAtualizacaoDTO dadosAtualizados) {
        return usuarioServices.atualizarUsuario(re, dadosAtualizados);
    }


    // Inativar usuario (transferencia)
    @PatchMapping("/inativar/{re}")
    public boolean inativarUsuario(@PathVariable String re) {
         return usuarioServices.inativarPolicialPorRe(re);
    }


    // Restaurar temporariamente a senha para o próprio RE e exigir uma nova senha pessoal
    @PatchMapping("/resetar-senha/{re}")
    public UsuarioRespostaDTO resetarSenha(@PathVariable String re) {
        return usuarioServices.resetarSenha(re);
    }

}
