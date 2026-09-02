package pmesp.helpdesk37bpmm.Chamado.enums;

public enum ChamadoStatus {
    ABERTO("Chamado Aberto"),
    FECHADO("Chamado Fechado"),
    EM_ATENDIMENTO("Chamado Em Atendimento"),
    CANCELADO("Chamado Cancelado");

    private final String descricao;

    ChamadoStatus(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
