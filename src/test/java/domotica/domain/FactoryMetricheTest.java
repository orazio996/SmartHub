package domotica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FactoryMetricheTest {

    private FactoryMetriche factory;

    @BeforeEach
    public void setup() {
        factory = new FactoryMetriche();
    }

    // TC01: testCreaCalcolatoreConsumo
    @Test
    public void testCreaCalcolatoreConsumo() {
        TipoMetrica tipoConsumo = new TipoMetrica("CoNsUmO", "Calcolo dei Consumi");
        
        CalcolatoreMetrica calcolatore = factory.creaCalcolatore(tipoConsumo);
        
        assertNotNull(calcolatore);
        assertTrue(calcolatore instanceof CalcolatoreConsumo, "La factory deve restituire un'istanza di CalcolatoreConsumo");
    }

    
    // TC02: testTipoMetricaNull
    @Test
    public void testTipoMetricaNull() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            factory.creaCalcolatore(null);
        });
        assertEquals("Impossibile creare un calcolatore per un TipoMetrica nullo.", e.getMessage());
    }

    
    // TC03: testMetricaNonSupportata
    @Test
    public void testMetricaNonSupportata() {
        TipoMetrica tipoSconosciuto = new TipoMetrica("Pressione", "Misura pressione");
        
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            factory.creaCalcolatore(tipoSconosciuto);
        });
        assertTrue(e.getMessage().contains("Nessun calcolatore supportato"));
    }
    
}