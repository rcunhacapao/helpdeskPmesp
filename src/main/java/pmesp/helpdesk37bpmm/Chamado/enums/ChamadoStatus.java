package pmesp.helpdesk37bpmm.Chamado.enums;

// O banco (ver migração V2) ainda permite os valores EM_DIAGNOSTICO e ABANDONADO por
// compatibilidade, mas eles não são mais usados: o diagnóstico do Mike IA hoje vive em
// AtendimentoMikeIA.resultado (null = em andamento), e um ChamadoModel só passa a
// existir quando o usuário envia o encaminhamento final para a equipe técnica.
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
