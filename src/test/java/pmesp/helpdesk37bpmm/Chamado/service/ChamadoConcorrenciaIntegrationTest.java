package pmesp.helpdesk37bpmm.Chamado.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.TestPropertySource;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoCategoria;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoPrioridade;
import pmesp.helpdesk37bpmm.Chamado.enums.ChamadoStatus;
import pmesp.helpdesk37bpmm.Chamado.model.ChamadoModel;
import pmesp.helpdesk37bpmm.Chamado.repository.ChamadoRepository;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.repository.TecnicoRepository;
import pmesp.helpdesk37bpmm.Usuario.enums.UsuarioPostoGraduacao;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

// Prova, com JPA de verdade (não mock), que dois técnicos não conseguem assumir o
// mesmo chamado silenciosamente: o segundo a salvar recebe um erro de conflito.
@SpringBootTest
@TestPropertySource(properties = {
        "DATABASE_URL=jdbc:h2:mem:helpdesk_concorrencia_test;DB_CLOSE_DELAY=-1",
        "DATABASE_USERNAME=sa",
        "DATABASE_PASSWORD="
})
class ChamadoConcorrenciaIntegrationTest {

    @Autowired
    private ChamadoRepository chamadoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private TecnicoRepository tecnicoRepository;

    @Test
    void deveRecusarASegundaGravacaoQuandoDoisTecnicosAssumemOMesmoChamadoAoMesmoTempo() {
        UsuarioModel solicitante = criarUsuario("300100", "Solicitante Teste", "solicitante.teste@policiamilitar.sp.gov.br");
        TecnicoModel tecnicoA = criarTecnico("300101", "Tecnico A", "tecnico.a@policiamilitar.sp.gov.br");
        TecnicoModel tecnicoB = criarTecnico("300102", "Tecnico B", "tecnico.b@policiamilitar.sp.gov.br");

        ChamadoModel chamado = new ChamadoModel();
        chamado.setSolicitante(solicitante);
        chamado.setDescricao("Computador não liga");
        chamado.setCategoria(ChamadoCategoria.COMPUTADOR);
        chamado.setLocalAtendimento("Sala de testes");
        chamado.setPrioridade(ChamadoPrioridade.MEDIA);
        chamado.setStatus(ChamadoStatus.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        Long chamadoId = chamadoRepository.save(chamado).getId();

        // Simula os dois técnicos lendo o mesmo chamado antes de qualquer um salvar
        ChamadoModel copiaDoTecnicoA = chamadoRepository.findById(chamadoId).orElseThrow();
        ChamadoModel copiaDoTecnicoB = chamadoRepository.findById(chamadoId).orElseThrow();

        copiaDoTecnicoA.setTecnicoResponsavel(tecnicoA);
        copiaDoTecnicoA.setStatus(ChamadoStatus.EM_ATENDIMENTO);
        chamadoRepository.save(copiaDoTecnicoA);

        copiaDoTecnicoB.setTecnicoResponsavel(tecnicoB);
        copiaDoTecnicoB.setStatus(ChamadoStatus.EM_ATENDIMENTO);
        assertThrows(OptimisticLockingFailureException.class, () -> chamadoRepository.save(copiaDoTecnicoB));
    }

    private UsuarioModel criarUsuario(String re, String nome, String email) {
        UsuarioModel usuario = new UsuarioModel();
        usuario.setRe(re);
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setPostoGraduacao(UsuarioPostoGraduacao.SD);
        usuario.setAtivo(true);
        usuario.setSenhaHash(re);
        return usuarioRepository.save(usuario);
    }

    private TecnicoModel criarTecnico(String re, String nome, String email) {
        UsuarioModel usuario = criarUsuario(re, nome, email);
        TecnicoModel tecnico = new TecnicoModel();
        tecnico.setUsuario(usuario);
        tecnico.setDisponivel(true);
        return tecnicoRepository.save(tecnico);
    }
}
