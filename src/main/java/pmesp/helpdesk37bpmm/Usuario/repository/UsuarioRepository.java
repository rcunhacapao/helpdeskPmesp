package pmesp.helpdesk37bpmm.Usuario.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByRe(String re);

    Optional<UsuarioModel> findByEmail(String email);

}
