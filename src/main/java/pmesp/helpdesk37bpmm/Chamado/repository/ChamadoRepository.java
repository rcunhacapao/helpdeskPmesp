package pmesp.helpdesk37bpmm.Chamado.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import java.util.List;

public interface ChamadoRepository extends JpaRepository<ChamadoModel, Long> {

    // Buscar chamados abertos de um usuário
    List<ChamadoModel> findBySolicitanteAndStatus(UsuarioModel solicitante, ChamadoStatus status);

    // Buscar chamados de um status do mais antigo para o mais recente
    List<ChamadoModel> findByStatusOrderByDataAberturaAsc(ChamadoStatus status);
}
