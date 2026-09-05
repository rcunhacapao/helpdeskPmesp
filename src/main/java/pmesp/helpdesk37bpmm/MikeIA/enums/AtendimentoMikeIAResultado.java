package pmesp.helpdesk37bpmm.MikeIA.enums;

// Como uma conversa com o Mike IA terminou
public enum AtendimentoMikeIAResultado {

    // O usuário confirmou que as sugestões resolveram o problema
    RESOLVIDO,

    // As orientações não resolveram; o formulário final criou um chamado para a equipe técnica
    ENCAMINHADO_PARA_CHAMADO,

    // O usuário deixou o diagnóstico sem concluir dentro do prazo definido.
    ABANDONADO
}
