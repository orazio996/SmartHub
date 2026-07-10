package domotica.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TipoMetricaTest {

    //TC01: testCostruttoreValido
    @Test
    public void testCreazioneValida() {
        TipoMetrica tipo = new TipoMetrica("Consumo Elettrico", "Misura in kWh");
        
        assertEquals("Consumo Elettrico", tipo.getNome());
        assertEquals("Misura in kWh", tipo.getDescrizione());
    }


    //TC02: testNomeNullo
    @Test
    public void testNomeNullo() {
        assertThrows(NullPointerException.class, ()->{
        	new TipoMetrica(null, "Descrizione valida");
    	});
    }
    
    
    // TC03: testNomeVuoto
    @Test
    public void testNomeVuoto() {
    	assertThrows(IllegalArgumentException.class, ()->{
    		new TipoMetrica("   ", "Descrizione valida");
    	});
    }

    
    // TC04: testDescrizioneNulla
    @Test
    public void testDescrizioneNulla() {
    	assertThrows(NullPointerException.class, ()->{
    		new TipoMetrica("Umidità", null);
    	});
    }
}
