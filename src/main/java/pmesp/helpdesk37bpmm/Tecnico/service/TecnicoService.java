package pmesp.helpdesk37bpmm.Tecnico.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Exception.ConflitoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoDTO;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoRespostaDTO;
import pmesp.helpdesk37bpmm.Tecnico.mapper.TecnicoMapper;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.repository.TecnicoRepository;
import pmesp.helpdesk37bpmm.Usuario.ValidadorDeRe;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TecnicoService {

    private final TecnicoRepository tecnicoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TecnicoMapper tecnicoMapper;

    public TecnicoRespostaDTO criar(TecnicoDTO tecnicoDTO) {
        if (tecnicoDTO == null) {
            throw new RegraDeNegocioException("DADOS_TECNICO_INVALIDOS",
                    "Envie os dados necessários para cadastrar o técnico.");
        }

        ValidadorDeRe.validar(tecnicoDTO.getRe());
        Optional<UsuarioModel> usuarioAtual = usuarioRepository.findByRe(tecnicoDTO.getRe());
        if (usuarioAtual.isEmpty()) {
            throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
        }

        UsuarioModel usuario = usuarioAtual.get();
        // Um usuário inativo não pode fazer parte da equipe técnica
        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("USUARIO_INATIVO",
                    "Não é possível cadastrar um usuário inativo como técnico.");
        }

        // Impedir que o mesmo usuário receba dois perfis de técnico
        if (tecnicoRepository.findByUsuario(usuario).isPresent()) {
            throw new ConflitoException("TECNICO_JA_CADASTRADO", "Este usuário já está cadastrado como técnico.");
        }

        TecnicoModel tecnicoNovo = new TecnicoModel();
        tecnicoNovo.setUsuario(usuario);
        tecnicoNovo.setDisponivel(false);

        return tecnicoMapper.map(tecnicoRepository.save(tecnicoNovo));
    }

    public TecnicoRespostaDTO buscarPorRe(String re) {
        return tecnicoMapper.map(buscarTecnicoPorRe(re));
    }

    public Optional<TecnicoModel> buscarPorReComoModel(String re) {
        ValidadorDeRe.validar(re);
        return tecnicoRepository.findByUsuarioRe(re);
    }

    public List<TecnicoRespostaDTO> listarTodosOsTecnicos() {
        List<TecnicoRespostaDTO> resposta = new ArrayList<>();
        for (TecnicoModel tecnico : tecnicoRepository.findAll()) {
            resposta.add(tecnicoMapper.map(tecnico));
        }
        return resposta;
    }

    public List<TecnicoRespostaDTO> listarTecnicosDisponiveis() {
        List<TecnicoRespostaDTO> resposta = new ArrayList<>();
        List<TecnicoModel> tecnicosDisponiveis = tecnicoRepository.findByDisponivelTrue();

        for (TecnicoModel tecnico : tecnicosDisponiveis) {
            if (tecnico.getUsuario().isAtivo()) {
                resposta.add(tecnicoMapper.map(tecnico));
            }
        }

        return resposta;
    }

    public TecnicoRespostaDTO ficarDisponivel(String re) {
        TecnicoModel tecnico = buscarTecnicoPorRe(re);

        if (!tecnico.getUsuario().isAtivo()) {
            throw new RegraDeNegocioException("TECNICO_INATIVO",
                    "Não é possível deixar um técnico inativo como disponível.");
        }

        tecnico.setDisponivel(true);
        return tecnicoMapper.map(tecnicoRepository.save(tecnico));
    }

    public TecnicoRespostaDTO ficarIndisponivel(String re) {
        TecnicoModel tecnico = buscarTecnicoPorRe(re);
        tecnico.setDisponivel(false);

        return tecnicoMapper.map(tecnicoRepository.save(tecnico));
    }

    public TecnicoModel buscarTecnicoDisponivelParaNovoChamado() {
        List<TecnicoModel> tecnicosDisponiveis = tecnicoRepository.findByDisponivelTrue();
        List<TecnicoModel> tecnicosAtivos = new ArrayList<>();

        for (TecnicoModel tecnico : tecnicosDisponiveis) {
            if (tecnico.getUsuario().isAtivo()) {
                tecnicosAtivos.add(tecnico);
            }
        }

        // Atribuir automaticamente apenas quando houver uma única opção
        if (tecnicosAtivos.size() == 1) {
            return tecnicosAtivos.get(0);
        }

        // Sem responsável automático quando não houver técnico ou houver mais de um
        return null;
    }

    public boolean estaDisponivel(TecnicoModel tecnico) {
        return tecnico.getUsuario().isAtivo() && tecnico.isDisponivel();
    }

    public Optional<TecnicoModel> buscarPorUsuario(UsuarioModel usuario) {
        return tecnicoRepository.findByUsuario(usuario);
    }

    public void tornarIndisponivel(TecnicoModel tecnico) {
        tecnico.setDisponivel(false);
        tecnicoRepository.save(tecnico);
    }

    private TecnicoModel buscarTecnicoPorRe(String re) {
        ValidadorDeRe.validar(re);
        Optional<TecnicoModel> tecnico = tecnicoRepository.findByUsuarioRe(re);

        if (tecnico.isPresent()) {
            return tecnico.get();
        }

        throw new RecursoNaoEncontradoException("TECNICO_NAO_ENCONTRADO", "Técnico não encontrado.");
    }
}
