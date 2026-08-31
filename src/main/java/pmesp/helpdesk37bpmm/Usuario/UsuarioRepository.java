package pmesp.helpdesk37bpmm.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByRe(String re);

    void deleteByRe(String re);
}
