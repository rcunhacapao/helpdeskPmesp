package pmesp.helpdesk37bpmm.Usuario;

import pmesp.helpdesk37bpmm.Exception.RegraDeNegocioException;

// Valida o formato do RE (Registro Estadual) nos três lugares que recebem esse dado
// (Usuario, Tecnico e Chamado), para a regra não ficar copiada em cada um.
public class ValidadorDeRe {

    private ValidadorDeRe() {
    }

    // O RE deve ser usado sempre sem o dígito verificador: só números, de 1 a 6 dígitos.
    public static void validar(String re) {
        if (re == null || !re.matches("[0-9]{1,6}")) {
            throw new RegraDeNegocioException("RE_INVALIDO", "Informe o RE sem o dígito.");
        }
    }
}
