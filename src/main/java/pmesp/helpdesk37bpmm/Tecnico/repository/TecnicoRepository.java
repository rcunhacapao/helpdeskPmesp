package pmesp.helpdesk37bpmm.Tecnico.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.util.List;
import java.util.Optional;

public interface TecnicoRepository extends JpaRepository<TecnicoModel, Long> {

    Optional<TecnicoModel> findByUsuarioRe(String re);

    Optional<TecnicoModel> findByUsuario(UsuarioModel usuario);

    List<TecnicoModel> findByDisponivelTrue();
}
