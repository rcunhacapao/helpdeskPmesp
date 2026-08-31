package pmesp.helpdesk37bpmm.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRespository extends JpaRepository<UsuarioModel, Long> {

    Optional<UsuarioModel> findByRe(Long re);

    boolean existsByRe(Long re);
    void deleteByRe(Long re);
}
