package pmesp.helpdesk37bpmm.Seguranca;

import org.junit.jupiter.api.Test;
import pmesp.helpdesk37bpmm.Exception.MuitasTentativasException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LimiteTentativasLoginTest {

    @Test
    void deveBloquearAOrigemEContaDepoisDoLimite() {
        LimiteTentativasLogin limite = new LimiteTentativasLogin();

        for (int tentativa = 0; tentativa < 5; tentativa++) {
            assertDoesNotThrow(() -> limite.consumirTentativa("192.0.2.10", "123456"));
        }

        assertThrows(MuitasTentativasException.class,
                () -> limite.consumirTentativa("192.0.2.10", "123456"));
    }

    @Test
    void deveLiberarNovoCicloDepoisDeAutenticacaoBemSucedida() {
        LimiteTentativasLogin limite = new LimiteTentativasLogin();
        limite.consumirTentativa("192.0.2.20", "654321");

        limite.registrarSucesso("192.0.2.20", "654321");

        assertDoesNotThrow(() -> limite.consumirTentativa("192.0.2.20", "654321"));
    }
}
