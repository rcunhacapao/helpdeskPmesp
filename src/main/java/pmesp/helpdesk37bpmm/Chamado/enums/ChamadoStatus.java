package pmesp.helpdesk37bpmm.Chamado.enums;

public enum ChamadoStatus {
    // O chamado já existe, mas ainda recebe o diagnóstico inicial do Mike IA.
    // Não entra na fila técnica enquanto permanecer neste estado.
    EM_DIAGNOSTICO("Em diagnóstico"),
    ABERTO("Chamado Aberto"),
    FECHADO("Chamado Fechado"),
    EM_ATENDIMENTO("Chamado Em Atendimento"),
    CANCELADO("Chamado Cancelado"),
    // Diagnóstico iniciado e deixado sem resposta pelo usuário após o prazo definido.
    ABANDONADO("Atendimento abandonado");

    private final String descricao;

    ChamadoStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
