package domotica.integration;

import domotica.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;

public class DescrizioniIntegrationTest {

    private DescDispositivo descLampada;

    @BeforeEach
    public void setup() {
        DescParametro paramLum = new DescParametro("luminosita", "int", "%", false, "0", "100", null);
        Map<String, DescParametro> mappa = new HashMap<>();
        mappa.put("luminosita", paramLum);
        descLampada = new DescDispositivo("Philips", "Luce", "Hue", mappa);
    }

    @Test
    public void testValidazione_ComandoValido() {
        Comando cmd = new Comando("luminosita", "50");
        assertTrue(descLampada.isCompatibile(cmd), "Il comando deve essere accettato");
    }

    @Test
    public void testValidazione_ComandoInvalido_FuoriRange() {
        Comando cmd = new Comando("luminosita", "150");
        assertFalse(descLampada.isCompatibile(cmd), "Il parametro deve bloccare il valore oltre 100");
    }
}