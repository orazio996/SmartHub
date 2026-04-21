 package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ParamStatoTest {

    // TC-01: Happy Path
    @Test
    public void testCostruttore_DatiValidi() {
        ParamStato param = new ParamStato("temperatura", "22.5");
        
        assertEquals("temperatura", param.getNome(), "Il nome deve corrispondere");
        assertEquals("22.5", param.getValore(), "Il valore deve corrispoendere");
    }

    // TC-02: Nome = null
    @Test
    public void testCostruttore_NomeNullo() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            new ParamStato(null, "22.5");
        });
        assertTrue(e.getMessage().contains("non può essere vuoto"));
    }

    // TC-03: Nome vuoto
    @Test
    public void testCostruttore_NomeVuoto() {
        assertThrows(IllegalArgumentException.class, () -> {
            new ParamStato("", "22.5");
        }, "Il nome non puo essere vuoto");

        assertThrows(IllegalArgumentException.class, () -> {
            new ParamStato("   ", "22.5");
        }, "Il nome non puo essere blank");
    }

    // TC-04: setValore happy path
    @Test
    public void testSetValore() {
        ParamStato param = new ParamStato("power", "OFF");

        param.setValore("ON");
        
        assertEquals("ON", param.getValore(), "Il vecchio valore deve essere sovrascritto");
    }
}