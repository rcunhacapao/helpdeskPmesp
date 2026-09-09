package pmesp.helpdesk37bpmm.RelatoErro.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Exception.AcessoNegadoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatarErroDTO;
import pmesp.helpdesk37bpmm.RelatoErro.dto.RelatoErroRespostaDTO;
import pmesp.helpdesk37bpmm.RelatoErro.enums.TipoDeErro;
import pmesp.helpdesk37bpmm.RelatoErro.mapper.RelatoErroMapper;
import pmesp.helpdesk37bpmm.RelatoErro.model.RelatoDeErro;
import pmesp.helpdesk37bpmm.RelatoErro.repository.RelatoErroRepository;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RelatoErroService {

    private final RelatoErroRepository relatoErroRepository;
    private final UsuarioRepository usuarioRepository;
    private final RelatoErroMapper relatoErroMapper;

    public RelatoErroRespostaDTO relatar(RelatarErroDTO relatarErroDTO) {
        if (relatarErroDTO == null || relatarErroDTO.getTipoErro() == null) {
            throw new RegraDeNegocioException("TIPO_DE_ERRO_OBRIGATORIO",
                    "Selecione o tipo do erro encontrado.");
        }
        if (relatarErroDTO.getTipoErro() == TipoDeErro.OUTRO
                && (relatarErroDTO.getObservacao() == null || relatarErroDTO.getObservacao().isBlank())) {
            throw new RegraDeNegocioException("OBSERVACAO_OBRIGATORIA_PARA_OUTRO",
                    "Descreva o erro encontrado.");
        }

        RelatoDeErro relato = new RelatoDeErro();
        relato.setUsuario(usuarioAutenticado());
        relato.setTipoErro(relatarErroDTO.getTipoErro());
        relato.setObservacao(relatarErroDTO.getObservacao() == null ? null : relatarErroDTO.getObservacao().trim());
        relato.setDataRelato(LocalDateTime.now());

        return relatoErroMapper.map(relatoErroRepository.save(relato));
    }

    public List<RelatoErroRespostaDTO> listarTodos() {
        if (!autenticadoEhTecnico()) {
            throw new AcessoNegadoException("RELATOS_DE_ERRO_RESTRITOS_A_TECNICO",
                    "Somente técnicos podem consultar os relatos de erro.");
        }

        List<RelatoErroRespostaDTO> resposta = new ArrayList<>();
        for (RelatoDeErro relato : relatoErroRepository.findAllByOrderByDataRelatoDesc()) {
            resposta.add(relatoErroMapper.map(relato));
        }
        return resposta;
    }

    private boolean autenticadoEhTecnico() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(autoridade -> autoridade.getAuthority().equals("ROLE_TECNICO"));
    }

    private UsuarioModel usuarioAutenticado() {
        String re = SecurityContextHolder.getContext().getAuthentication().getName();
        return usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));
    }
}
