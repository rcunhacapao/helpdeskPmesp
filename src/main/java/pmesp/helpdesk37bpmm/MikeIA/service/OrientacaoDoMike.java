package pmesp.helpdesk37bpmm.MikeIA.service;

// Resultado da busca de orientação. Além do texto, deixa explícito se existe uma
// instrução prática que o usuário possa testar antes de confirmar uma resolução.
public record OrientacaoDoMike(String sugestoes, boolean possuiOrientacaoTestavel) {
}
