package pmesp.helpdesk37bpmm.Chamado;

import org.springframework.data.jpa.repository.JpaRepository;
import pmesp.helpdesk37bpmm.Usuario.UsuarioModel;

import java.util.List;

public interface ChamadoRepository extends JpaRepository<ChamadoModel, Long> {

    // Buscar chamados abertos de um usuário
    List<ChamadoModel> findBySolicitanteAndStatus(UsuarioModel solicitante, ChamadoStatus status);
}
