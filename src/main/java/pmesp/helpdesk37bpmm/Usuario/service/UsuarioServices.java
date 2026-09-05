package pmesp.helpdesk37bpmm.Usuario.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pmesp.helpdesk37bpmm.Chamado.service.ChamadoService;
import pmesp.helpdesk37bpmm.Exception.ConflitoException;
import pmesp.helpdesk37bpmm.Exception.RecursoNaoEncontradoException;
import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;
import pmesp.helpdesk37bpmm.Tecnico.model.TecnicoModel;
import pmesp.helpdesk37bpmm.Tecnico.service.TecnicoService;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioAtualizacaoDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioDTO;
import pmesp.helpdesk37bpmm.Usuario.dto.UsuarioRespostaDTO;
import pmesp.helpdesk37bpmm.Usuario.mapper.UsuarioMapper;
import pmesp.helpdesk37bpmm.Usuario.model.UsuarioModel;
import pmesp.helpdesk37bpmm.Usuario.repository.UsuarioRepository;

import java.util.Optional;

@Service
public class UsuarioServices {

    @Autowired
    UsuarioRepository usuarioRespository;

    @Autowired
    ChamadoService chamadoService;

    @Autowired
    TecnicoService tecnicoService;

    @Autowired
    UsuarioMapper usuarioMapper;

    // Cadastrar novo usuario
    public UsuarioRespostaDTO criar(UsuarioDTO usuarioDTO) {
        // Conferir se o corpo do cadastro foi enviado
        if (usuarioDTO == null) {
            throw new RegraDeNegocioException("DADOS_USUARIO_INVALIDOS",
                    "Envie os dados necessários para cadastrar o usuário.");
        }

        // Conferir os dados que o usuário precisa preencher
        validarRe(usuarioDTO.getRe());

        if (usuarioDTO.getNome() == null || usuarioDTO.getNome().isBlank()) {
            throw new RegraDeNegocioException("NOME_USUARIO_OBRIGATORIO", "Informe o nome do usuário.");
        }

        if (usuarioDTO.getPostoGraduacao() == null) {
            throw new RegraDeNegocioException("POSTO_GRADUACAO_OBRIGATORIO",
                    "Informe o posto ou graduação do usuário.");
        }

        // Impedir dois cadastros usando o mesmo RE
        if (usuarioRespository.findByRe(usuarioDTO.getRe()).isPresent()) {
            throw new ConflitoException("RE_JA_CADASTRADO", "Já existe um usuário cadastrado com este RE.");
        }


        // Transformar os dados do cadastro em usuario para salvar no banco
        UsuarioModel usuarioNovo = usuarioMapper.map(usuarioDTO);

        // Todo novo usuario começa ativo
        usuarioNovo.setAtivo(true);

        // Transformar o usuario salvo em resposta para a API
        return usuarioMapper.map(usuarioRespository.save(usuarioNovo));
    }


    // Pesquisar usuario por RE
    public UsuarioRespostaDTO buscarPorRe(String re)  {
        // Conferir se o RE informado pode ser pesquisado
        validarRe(re);
        Optional<UsuarioModel> buscarRe = usuarioRespository.findByRe(re);
        if (buscarRe.isPresent()) {
            return usuarioMapper.map(buscarRe.get());
        }
        throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
    }


    // Inativar usuario por RE (Transferencia de BTL)
    public boolean inativarPolicialPorRe(String re) {
        // Conferir o RE antes de procurar o usuário
        validarRe(re);
        Optional<UsuarioModel> policial = usuarioRespository.findByRe(re);
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
            usuarioRespository.save(usuario);
            return true;
        }
        throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
    }


    // Atualizar apenas nome e posto/graduação do usuario
    public UsuarioRespostaDTO atualizarUsuario(String re, UsuarioAtualizacaoDTO dadosAtualizados) {
        // Conferir o RE e os novos dados antes de atualizar no banco
        validarRe(re);

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

        Optional<UsuarioModel> usuarioAtual = usuarioRespository.findByRe(re);
        if (usuarioAtual.isPresent()) {
            UsuarioModel usuario = usuarioAtual.get();
            usuario.setNome(dadosAtualizados.getNome());
            usuario.setPostoGraduacao(dadosAtualizados.getPostoGraduacao());

            // Transformar o usuario atualizado em resposta para a API
            return usuarioMapper.map(usuarioRespository.save(usuario));
        }
        throw new RecursoNaoEncontradoException("USUARIO_NAO_ENCONTRADO", "Usuário não encontrado.");
    }


    // Validar o RE informado sem o dígito
    private void validarRe(String re) {
        if (re == null || !re.matches("[0-9]{1,6}")) {
            throw new RegraDeNegocioException("RE_INVALIDO", "Informe o RE sem o dígito.");
        }
    }
}
