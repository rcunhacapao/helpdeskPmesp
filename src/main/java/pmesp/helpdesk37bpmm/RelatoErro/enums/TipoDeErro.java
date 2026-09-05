package pmesp.helpdesk37bpmm.RelatoErro.enums;

// Categorias de erro que a pessoa pode relatar sobre o sistema em si (não sobre o
// equipamento dela). OUTRO exige uma observação descrevendo o problema.
public enum TipoDeErro {

    LAYOUT_QUEBRADO,
    BOTAO_OU_LINK_NAO_FUNCIONA,
    DADO_NAO_SALVOU_OU_CARREGOU,
    MENSAGEM_DE_ERRO_INESPERADA,
    LENTIDAO,
    OUTRO
}
