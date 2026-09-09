package pmesp.helpdesk37bpmm.Usuario.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pmesp.helpdesk37bpmm.Chamado.service.ChamadoService;
import pmesp.helpdesk37bpmm.Exception.ConflitoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;
import pmesp.helpdesk37bpmm.Usuario.ValidadorDeRe;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioAtualizacaoDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO;
import pmesp.helpdesk37bpmm.Usuario.mapper.UsuarioMapper;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioServices {

    private final UsuarioRepository usuarioRepository;
    private final ChamadoService chamadoService;
    private final TecnicoService tecnicoService;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;

    public UsuarioRespostaDTO criar(UsuarioDTO usuarioDTO) {
        if (usuarioDTO == null) {
            throw new RegraDeNegocioException("DADOS_USUARIO_INVALIDOS",
                    "Envie os dados necessários para cadastrar o usuário.");
        }

        ValidadorDeRe.validar(usuarioDTO.getRe());

        if (usuarioDTO.getNome() == null || usuarioDTO.getNome().isBlank()) {
            throw new RegraDeNegocioException("NOME_USUARIO_OBRIGATORIO", "Informe o nome do usuário.");
        }

        if (usuarioDTO.getPostoGraduacao() == null) {
            throw new RegraDeNegocioException("POSTO_GRADUACAO_OBRIGATORIO",
                    "Informe o posto ou graduação do usuário.");
        }

        String email = normalizarEmail(usuarioDTO.getEmail());
        validarEmailFuncional(email);

        // Impedir dois cadastros usando o mesmo RE
        if (usuarioRepository.findByRe(usuarioDTO.getRe()).isPresent()) {
            throw new ConflitoException("RE_JA_CADASTRADO", "Já existe um usuário cadastrado com este RE.");
        }

        // Impedir dois cadastros usando o mesmo e-mail funcional
        if (email != null && usuarioRepository.findByEmail(email).isPresent()) {
            throw new ConflitoException("EMAIL_JA_CADASTRADO", "Já existe um usuário cadastrado com este e-mail.");
        }


        UsuarioModel usuarioNovo = usuarioMapper.map(usuarioDTO);
        usuarioNovo.setEmail(email);

        // Todo novo usuário começa ativo e entra temporariamente com RE/RE.
        usuarioNovo.setAtivo(true);
        usuarioNovo.setSenhaHash(passwordEncoder.encode(usuarioNovo.getRe()));
        usuarioNovo.setTrocaSenhaObrigatoria(true);

        return usuarioMapper.map(usuarioRepository.save(usuarioNovo));
    }


    public UsuarioRespostaDTO buscarPorRe(String re)  {
        ValidadorDeRe.validar(re);
        Optional<UsuarioModel> buscarRe = usuarioRepository.findByRe(re);
        if (buscarRe.isPresent()) {
            return usuarioMapper.map(buscarRe.get());
        }
        throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
    }


    // Inativar usuario por RE (Transferencia de BTL)
    // @Transactional garante que inativar o usuário, tornar o técnico indisponível e cancelar
    // os chamados abertos aconteçam como uma única operação: se algo falhar no meio do caminho,
    // nada fica salvo pela metade.
    @Transactional
    public boolean inativarPolicialPorRe(String re) {
        ValidadorDeRe.validar(re);
        Optional<UsuarioModel> policial = usuarioRepository.findByRe(re);
        if (policial.isPresent()) {
            UsuarioModel usuario = policial.get();
            Optional<TecnicoModel> tecnico = tecnicoService.buscarPorUsuario(usuario);

            // Não inativar técnico que ainda tem chamados para atender
            if (tecnico.isPresent() && chamadoService.temChamadosPendentesDoTecnico(tecnico.get())) {
                throw new RegraDeNegocioException("TECNICO_COM_CHAMADOS_PENDENTES",
                        "Não é possível inativar este técnico porque ele possui chamados pendentes.");
            }

            // Inativar o usuário, retirar a disponibilidade técnica e cancelar chamados abertos
            usuario.setAtivo(false);
            if (tecnico.isPresent()) {
                tecnicoService.tornarIndisponivel(tecnico.get());
            }
            chamadoService.cancelarChamadosAbertosDoUsuario(usuario);
            usuarioRepository.save(usuario);
            return true;
        }
        throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
    }


    public UsuarioRespostaDTO atualizarUsuario(String re, UsuarioAtualizacaoDTO dadosAtualizados) {
        ValidadorDeRe.validar(re);

        if (dadosAtualizados == null) {
            throw new RegraDeNegocioException("DADOS_USUARIO_INVALIDOS",
                    "Envie os dados necessários para atualizar o usuário.");
        }

        if (dadosAtualizados.getNome() == null || dadosAtualizados.getNome().isBlank()) {
            throw new RegraDeNegocioException("NOME_USUARIO_OBRIGATORIO", "Informe o nome do usuário.");
        }

        if (dadosAtualizados.getPostoGraduacao() == null) {
            throw new RegraDeNegocioException("POSTO_GRADUACAO_OBRIGATORIO",
                    "Informe o posto ou graduação do usuário.");
        }

        Optional<UsuarioModel> usuarioAtual = usuarioRepository.findByRe(re);
        if (usuarioAtual.isPresent()) {
            UsuarioModel usuario = usuarioAtual.get();
            usuario.setNome(dadosAtualizados.getNome());
            usuario.setPostoGraduacao(dadosAtualizados.getPostoGraduacao());

            return usuarioMapper.map(usuarioRepository.save(usuario));
        }
        throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
    }


    // O reset técnico devolve a conta ao mesmo estado seguro de um novo cadastro.
    public UsuarioRespostaDTO resetarSenha(String re) {
        ValidadorDeRe.validar(re);
        UsuarioModel usuario = usuarioRepository.findByRe(re)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "USUARIO_NAO_ENCONTRADO", "Usuário não encontrado."));

        if (!usuario.isAtivo()) {
            throw new RegraDeNegocioException("USUARIO_INATIVO",
                    "Não é possível resetar a senha de um usuário inativo.");
        }

        usuario.setSenhaHash(passwordEncoder.encode(usuario.getRe()));
        usuario.setTrocaSenhaObrigatoria(true);
        return usuarioMapper.map(usuarioRepository.save(usuario));
    }


    // O e-mail é opcional, mas continua seguindo o domínio institucional quando informado.
    private void validarEmailFuncional(String email) {
        if (email != null && !email.toLowerCase().endsWith("@policiamilitar.sp.gov.br")) {
            throw new RegraDeNegocioException("EMAIL_FUNCIONAL_INVALIDO",
                    "Informe um e-mail funcional válido, terminado em @policiamilitar.sp.gov.br.");
        }
    }

    private String normalizarEmail(String email) {
        return email == null || email.isBlank() ? null : email.trim().toLowerCase();
    }
}
