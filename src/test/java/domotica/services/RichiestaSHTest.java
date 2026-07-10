package domotica.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import domotica.domain.ComandoSingolo;

public class RichiestaSHTest {

    // TEST 1: Happy Path
    @Test
    public void testCostruttore_DatiValidi() {

        ComandoSingolo comando = new ComandoSingolo("power", "ON");

        RichiestaSH richiesta = new RichiestaSH("sourceTarget", "source", "tipo", System.currentTimeMillis(), comando);
        
        assertEquals("tipo", richiesta.getTipo(), "Il tipo deve coincidere");
        assertEquals("power", richiesta.getParam(), "Il parametro deve coincidere");
        assertEquals("ON", richiesta.getVal(), "Il comando deve coincidere");
    }

    // TEST 2: tipo = null
    @Test
    public void testCostruttore_TipoNullo() {
        ComandoSingolo comando = new ComandoSingolo("power", "ON");
        
        assertThrows(NullPointerException.class, () -> {
        	new RichiestaSH("sourceTarget", "source", null, System.currentTimeMillis(), comando);
        }, "Deve lanciare eccezione se il tipo è null");
    }

    // TEST 3: tipo vuoto
    @Test
    public void testCostruttore_TipoVuoto() {
        ComandoSingolo comando = new ComandoSingolo("power", "ON");

        assertThrows(IllegalArgumentException.class, () -> {
        	new RichiestaSH("sourceTarget", "source", "", System.currentTimeMillis(), comando);
        }, "Deve lanciare eccezione se il tipo è vuoto");
        
        assertThrows(IllegalArgumentException.class, () -> {
        	new RichiestaSH("sourceTarget", "source", "  ", System.currentTimeMillis(), comando);
        }, "Deve lanciare eccezione se il tipo contiene solo spazi");
    }

    // TEST 4: comando = null
    @Test
    public void testCostruttore_ComandoNullo() {
        assertThrows(NullPointerException.class, () -> {
        	new RichiestaSH("sourceTarget", "source", "tipo", System.currentTimeMillis(), null);
        }, "Deve lanciare eccezione se il comando è null");
    }
    
 // TEST 5: source = null
    @Test
    public void testCostruttore_sourceNullo() {
        ComandoSingolo comando = new ComandoSingolo("power", "ON");
        
        assertThrows(NullPointerException.class, () -> {
        	new RichiestaSH("sourceTarget", null, "tipo", System.currentTimeMillis(), comando);
        }, "Deve lanciare eccezione se il source è null");
    }
    
 // TEST 6: source vuoto
    @Test
    public void testCostruttore_sourceVuoto() {
        ComandoSingolo comando = new ComandoSingolo("power", "ON");
        
        assertThrows(IllegalArgumentException.class, () -> {
        	new RichiestaSH("sourceTarget", "", "tipo", System.currentTimeMillis(), comando);
        }, "Deve lanciare eccezione se il source è vuoto");
        assertThrows(IllegalArgumentException.class, () -> {
        	new RichiestaSH("sourceTarget", "  ", "tipo", System.currentTimeMillis(), comando);
        }, "Deve lanciare eccezione se il source è vuoto");
    }
    
 // TEST 7: sourceTimestamp negativo
    @Test
    public void testCostruttore_sourceTimestampNegativo() {
        ComandoSingolo comando = new ComandoSingolo("power", "ON");
        
        assertThrows(IllegalArgumentException.class, () -> {
        	new RichiestaSH("sourceTarget", "source", "tipo", -1, comando);
        }, "Deve lanciare eccezione se il sourceTimestamp è negativo");
    }

    // TEST 8: validazione formato json del toString()
    @Test
    public void testToString_FormattaJSONCorrettamente() {

        ComandoSingolo comando = new ComandoSingolo("luminosita", "80");
        RichiestaSH richiesta = new RichiestaSH("sourceTarget", "source", "tipo", System.currentTimeMillis(), comando);

        String jsonAtteso = String.format("{\"sourceTarget\":\"sourceTarget\", \"source\":\"source\",\"tipo\":\"tipo\", \"sourceTimestamp\":\"%d\", \"param\":\"luminosita\", \"val\":\"80\"}", 
                System.currentTimeMillis());

        assertEquals(jsonAtteso, richiesta.toString(), "Il formato JSON generato dal toString non è corretto");
    }
}