package pmesp.helpdesk37bpmm.Usuario.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UsuarioPostoGraduacao {

    SD("SD PM"),
    CB("CB PM"),
    SGT_3("3° SGT PM"),
    SGT_2("2° SGT PM"),
    SGT_1("1° SGT PM"),
    SUBTEN("SUBTEN PM"),
    ASP_OF("ASP OF PM"),
    TEN_2("2° TEN PM"),
    TEN_1("1° TEN PM"),
    CAP("CAP PM"),
    MAJ("MAJ PM"),
    TEN_CEL("TEN CEL PM"),
    CEL("CEL PM");

    private final String descricao;

    UsuarioPostoGraduacao(String descricao) {
        this.descricao = descricao;
    }

    @JsonValue
    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return this.descricao;
    }
}
