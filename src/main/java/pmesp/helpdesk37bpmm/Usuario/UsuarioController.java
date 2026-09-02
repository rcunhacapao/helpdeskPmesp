package pmesp.helpdesk37bpmm.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioServices usuarioServices;

    // Cadastrar usuario
    @PostMapping("/cadastrar")
    public UsuarioRespostaDTO cadastrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return usuarioServices.criar(usuarioDTO);
    }


    // Buscar por RE
    @GetMapping("/buscar/{re}")
    public UsuarioRespostaDTO buscarUsuarioPorRe(@PathVariable String re) {
        return usuarioServices.buscarPorRe(re);
    }


    // Atualizar dados
    @PutMapping("/atualizar-dados/{re}")
    public UsuarioRespostaDTO atualizarUsuario(@PathVariable String re, @RequestBody UsuarioAtualizacaoDTO dadosAtualizados) {
        return usuarioServices.atualizarUsuario(re, dadosAtualizados);
    }


    // Inativar usuario (transferencia)
    @PatchMapping("/inativar/{re}")
    public boolean inativarUsuario(@PathVariable String re) {
         return usuarioServices.inativarPolicialPorRe(re);
    }

}
