package pmesp.helpdesk37bpmm.Tecnico.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoDTO;
import pmesp.helpdesk37bpmm.Tecnico.dto.TecnicoRespostaDTO;
import pmesp.helpdesk37bpmm.Tecnico.mapper.TecnicoMapper;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.repository.TecnicoRepository;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TecnicoService {

    @Autowired
    TecnicoRepository tecnicoRepository;
    @Autowired
    UsuarioRepository usuarioRepository;
    @Autowired
    TecnicoMapper tecnicoMapper;

    // Cadastrar um usuário ativo como técnico da equipe
    public TecnicoRespostaDTO criar(TecnicoDTO tecnicoDTO) {
        if (tecnicoDTO == null || tecnicoDTO.getRe() == null || tecnicoDTO.getRe().isBlank()) {
            return null;
        }

        Optional<UsuarioModel> usuarioAtual = usuarioRepository.findByRe(tecnicoDTO.getRe());
        if (usuarioAtual.isPresent() && !tecnicoRepository.findByUsuario(usuarioAtual.get()).isPresent()) {
            UsuarioModel usuario = usuarioAtual.get();

            if (usuario.isAtivo()) {
                // Criar o perfil técnico para o usuário encontrado pelo RE
                TecnicoModel tecnicoNovo = new TecnicoModel();
                tecnicoNovo.setUsuario(usuario);
                tecnicoNovo.setDisponivel(false);

                return tecnicoMapper.map(tecnicoRepository.save(tecnicoNovo));
            }
        }
        return null;
    }


    // Buscar técnico pelo RE do usuário relacionado
    public TecnicoRespostaDTO buscarPorRe(String re) {
        Optional<TecnicoModel> tecnico = tecnicoRepository.findByUsuarioRe(re);
        if (tecnico.isPresent()) {
            return tecnicoMapper.map(tecnico.get());
        }
        return null;
    }


    // Buscar técnico pelo RE para regras de chamado
    public Optional<TecnicoModel> buscarPorReComoModel(String re) {
        return tecnicoRepository.findByUsuarioRe(re);
    }


    // Mostrar somente técnicos que se colocaram disponíveis
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


    // Permitir que o técnico receba chamados neste momento
    public TecnicoRespostaDTO ficarDisponivel(String re) {
        Optional<TecnicoModel> tecnicoAtual = tecnicoRepository.findByUsuarioRe(re);
        if (tecnicoAtual.isPresent()) {
            TecnicoModel tecnico = tecnicoAtual.get();

            if (tecnico.getUsuario().isAtivo()) {
                tecnico.setDisponivel(true);
                return tecnicoMapper.map(tecnicoRepository.save(tecnico));
            }
        }
        return null;
    }


    // Parar de receber novos chamados neste momento
    public TecnicoRespostaDTO ficarIndisponivel(String re) {
        Optional<TecnicoModel> tecnicoAtual = tecnicoRepository.findByUsuarioRe(re);
        if (tecnicoAtual.isPresent()) {
            TecnicoModel tecnico = tecnicoAtual.get();
            tecnico.setDisponivel(false);

            return tecnicoMapper.map(tecnicoRepository.save(tecnico));
        }
        return null;
    }


    // Buscar responsável automático quando houver somente um técnico disponível
    public TecnicoModel buscarTecnicoDisponivelParaNovoChamado() {
        List<TecnicoModel> tecnicosDisponiveis = tecnicoRepository.findByDisponivelTrue();
        List<TecnicoModel> tecnicosAtivos = new ArrayList<>();

        for (TecnicoModel tecnico : tecnicosDisponiveis) {
            if (tecnico.getUsuario().isAtivo()) {
                tecnicosAtivos.add(tecnico);
            }
        }

        if (tecnicosAtivos.size() == 1) {
            return tecnicosAtivos.get(0);
        }

        return null;
    }


    // Confirmar se técnico está disponível e ativo
    public boolean estaDisponivel(TecnicoModel tecnico) {
        return tecnico.getUsuario().isAtivo() && tecnico.isDisponivel();
    }


    // Buscar técnico pelo usuário para validar regras de inativação
    public Optional<TecnicoModel> buscarPorUsuario(UsuarioModel usuario) {
        return tecnicoRepository.findByUsuario(usuario);
    }


    // Tornar técnico indisponível ao inativar o usuário relacionado
    public void tornarIndisponivel(TecnicoModel tecnico) {
        tecnico.setDisponivel(false);
        tecnicoRepository.save(tecnico);
    }
}
