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
    public UsuarioModel cadastrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        return usuarioServices.criar(usuarioDTO);
    }


    // Buscar por RE
    @GetMapping("/buscar/{re}")
    public UsuarioModel buscarUsuarioPorRe(@PathVariable String re) {
        return usuarioServices.buscarPorRe(re);
    }


    // Atualizar dados
    @PutMapping("/atualizar-dados/{re}")
    public UsuarioModel atualizarUsuario(@PathVariable String re, @RequestBody UsuarioModel dadosAtualizados) {
        return usuarioServices.atualizarUsuario(re, dadosAtualizados);
    }


    // Inativar usuario (transferencia)
    @PatchMapping("/inativar/{re}")
    public boolean inativarUsuario(@PathVariable String re) {
         return usuarioServices.inativarPolicialPorRe(re);
    }

}
