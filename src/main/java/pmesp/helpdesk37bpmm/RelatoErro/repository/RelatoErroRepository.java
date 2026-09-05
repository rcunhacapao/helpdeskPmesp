package pmesp.helpdesk37bpmm.RelatoErro.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pmesp.helpdesk37bpmm.RelatoErro.model.RelatoDeErro;

import java.util.List;

public interface RelatoErroRepository extends JpaRepository<RelatoDeErro, Long> {

    List<RelatoDeErro> findAllByOrderByDataRelatoDesc();
}
