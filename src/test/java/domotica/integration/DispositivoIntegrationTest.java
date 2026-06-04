package domotica.integration;

import domotica.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispositivoIntegrationTest {

    private Dispositivo dispositivo;

    @BeforeEach
    public void setup() {
        DescParametro paramLum = new DescParametro("luminosita", "int", "%", false, "0", "100", null);
        Map<String, DescParametro> mappa = new HashMap<>();
        mappa.put("luminosita", paramLum);
        DescDispositivo descLampada = new DescDispositivo("Philips", "Luce", "Hue", mappa);

        dispositivo = new Dispositivo("Luce_Soggiorno", "192.168.1.10", descLampada);
    }

    @Test
    public void testRicerca_DispositivoCompatibile() {
        Comando cmd = new Comando("luminosita", "80");
        List<Dispositivo> trovati = dispositivo.getDispositiviCompatibili(cmd);
        
        assertEquals(1, trovati.size());
        assertEquals("Luce_Soggiorno", trovati.get(0).getId());
    }

    @Test
    public void testRicerca_DispositivoIncompatibile() {
        Comando cmd = new Comando("volume", "50"); // Errato!
        List<Dispositivo> trovati = dispositivo.getDispositiviCompatibili(cmd);
        
        assertTrue(trovati.isEmpty(), "Deve restituire lista vuota se incompatibile");
    }
}