package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DescDispositivoTest {

    private Map<String, DescParametro> mappaBase;

    @BeforeEach
    public void setup() {
        mappaBase = new HashMap<>();
        // Creiamo un parametro reale di supporto per i test
        DescParametro paramLuce = new DescParametro("power", "string", "", false, "", "", List.of("ON", "OFF"));
        mappaBase.put("power", paramLuce);
    }

    // TC-01
    @Test
    public void testCostruttore_MappaVuotaONulla() {
        assertThrows(IllegalArgumentException.class, () -> new DescDispositivo("marca", "tipo", "modello", null));
        assertThrows(IllegalArgumentException.class, () -> new DescDispositivo("marca", "tipo", "modello", new HashMap<>()));
    }

    
    // TC-02
    @Test
    public void testMappaDifensiva() {
        DescDispositivo desc = new DescDispositivo("marca", "tipo", "modello", mappaBase);
        
        desc.getDescParametri().clear();

        assertFalse(desc.getDescParametri().isEmpty(), "Il getter deve restituire una copia difensiva");
    }

    // TC-03
    @Test
    public void testIsCompatibile_ComandoNulloOInesistente() {
        DescDispositivo desc = new DescDispositivo("marca", "tipo", "modello", mappaBase);
        
        assertFalse(desc.isCompatibile(null), "Comando nullo deve essere rifiutato");
        assertFalse(desc.isCompatibile(new Comando("volume", "50")), "Parametro non in mappa deve essere rifiutato");
    }

    @Test
    public void testIsCompatibile_HappyPath() {
        DescDispositivo desc = new DescDispositivo("marca", "tipo", "modello", mappaBase);
        
        assertTrue(desc.isCompatibile(new Comando("power", "ON")), "Valore valido -> true");
        assertFalse(desc.isCompatibile(new Comando("power", "STANDBY")), "Valore non valido -> false");
    }
}