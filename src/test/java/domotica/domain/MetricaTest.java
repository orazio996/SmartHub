package domotica.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MetricaTest {

    private TipoMetrica tipoValido = new TipoMetrica("Consumo", "Consumo elettrico");

    
    // TC01: testCreazioneValida
    @Test
    public void testCreazioneValida() {
        Metrica m = new Metrica(150.5, "kWh", tipoValido);
        
        assertEquals(150.5, m.getValore(), 0.001); // Il terzo parametro è la tolleranza per i double
        assertEquals("kWh", m.getUnitaMisura());
        assertEquals(tipoValido, m.getTipoMetrica());
    }

    
    // TC02: testValoreNaN
    @Test
    public void testValoreNaN() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Metrica(Double.NaN, "kWh", tipoValido);
        });
    }

    
    //TC03: testUnitaMisuraNulla
    @Test
    public void testUnitaMisuraNulla() {
        assertThrows(NullPointerException.class, () -> {
            new Metrica(100.0, null, tipoValido);
        });
    }

    
    // TC04: testUnitaMisuraVuota
    @Test
    public void testUnitaMisuraVuota() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Metrica(100.0, "   ", tipoValido);
        });
    }

    
    // TC05: testTipoMetricaNullo
    @Test
    public void testTipoMetricaNullo() {
        assertThrows(NullPointerException.class, () -> {
            new Metrica(100.0, "kWh", null);
        });
    }
}