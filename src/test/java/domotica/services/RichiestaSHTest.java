package domotica.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import domotica.domain.Comando;

public class RichiestaSHTest {

    // TEST 1: Happy Path
    @Test
    public void testCostruttore_DatiValidi() {

        Comando comando = new Comando("power", "ON");

        RichiestaSH richiesta = new RichiestaSH("cmd", comando);
        
        assertEquals("cmd", richiesta.getTipo(), "Il tipo deve coincidere");
        assertEquals("power", richiesta.getParam(), "Il parametro deve coincidere");
        assertEquals("ON", richiesta.getVal(), "Il comando deve coincidere");
    }

    // TEST 2: tipo = null
    @Test
    public void testCostruttore_TipoNullo() {
        Comando comando = new Comando("power", "ON");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new RichiestaSH(null, comando);
        }, "Deve lanciare eccezione se il tipo è null");
    }

    // TEST 3: tipo vuoto
    @Test
    public void testCostruttore_TipoVuoto() {
        Comando comando = new Comando("power", "ON");

        assertThrows(IllegalArgumentException.class, () -> {
            new RichiestaSH("", comando);
        }, "Deve lanciare eccezione se il tipo è vuoto");
        
        assertThrows(IllegalArgumentException.class, () -> {
            new RichiestaSH("   ", comando);
        }, "Deve lanciare eccezione se il tipo contiene solo spazi");
    }

    // TEST 4: comando = null
    @Test
    public void testCostruttore_ComandoNullo() {
        assertThrows(IllegalArgumentException.class, () -> {
            new RichiestaSH("cmd", null);
        }, "Deve lanciare eccezione se il comando è null");
    }

    // TEST 5: validazione formato json del toString()
    @Test
    public void testToString_FormattaJSONCorrettamente() {

        Comando comando = new Comando("luminosita", "80");
        RichiestaSH richiesta = new RichiestaSH("cmd", comando);

        String jsonAtteso = "{\"tipo\":\"cmd\", \"param\":\"luminosita\", \"val\":\"80\"}";

        assertEquals(jsonAtteso, richiesta.toString(), "Il formato JSON generato dal toString non è corretto");
    }
}