package pmesp.helpdesk37bpmm.MikeIA.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import pmesp.helpdesk37bpmm.MikeIA.model.AtendimentoMikeIA;
import pmesp.helpdesk37bpmm.MikeIA.enums.AtendimentoMikeIAResultado;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface AtendimentoMikeIARepository extends JpaRepository<AtendimentoMikeIA, Long> {

    Optional<AtendimentoMikeIA> findByChamadoId(Long chamadoId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select atendimento from AtendimentoMikeIA atendimento where atendimento.id = :id")
    Optional<AtendimentoMikeIA> buscarPorIdParaAtualizacao(@Param("id") Long id);

    Optional<AtendimentoMikeIA> findFirstByUsuarioAndResultadoIsNullOrderByDataInicioDesc(UsuarioModel usuario);

    List<AtendimentoMikeIA> findByResultadoIsNullAndDataInicioBefore(LocalDateTime limiteDeInatividade);

    long countByResultado(AtendimentoMikeIAResultado resultado);
}
