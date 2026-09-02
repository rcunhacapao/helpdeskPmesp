package pmesp.helpdesk37bpmm.Chamado.enums;

public enum ChamadoPrioridade {
    BAIXA("Baixa - até 1 Hora"),
    MEDIA("Media - até 40 minutos"),
    ALTA("Alta - até 15 minutos"),
    URGENTE("Urgente - imediato");

    private final String descricaoDoChamado;

    ChamadoPrioridade(String descricaoDoChamado) {
        this.descricaoDoChamado = descricaoDoChamado;
    }

    public String getDescricaoDoChamado() {
        return descricaoDoChamado;
    }

}
