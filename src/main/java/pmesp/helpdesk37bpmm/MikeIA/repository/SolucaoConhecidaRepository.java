package pmesp.helpdesk37bpmm.MikeIA.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.MikeIA.model.SolucaoConhecida;

import java.util.List;

public interface SolucaoConhecidaRepository extends JpaRepository<SolucaoConhecida, Long> {

    // Buscar soluções ativas de uma categoria, na ordem definida para exibição
    List<SolucaoConhecida> findByCategoriaAndAtivoTrueOrderByOrdemAsc(ChamadoCategoria categoria);

    // Buscar todas as soluções ativas, usado quando o usuário não informa categoria
    List<SolucaoConhecida> findByAtivoTrueOrderByOrdemAsc();
}
