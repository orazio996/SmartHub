package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ComandoTest {

	// TC-01: test costruttore happy path
    @Test
    public void testCostruttore_HappyPath() {
        Comando comando = new Comando("luminosita", "80");
        
        assertEquals("luminosita", comando.getParam());
        assertEquals("80", comando.getValore());
    }

    
    // TC-02: test costruttore comando invalido
    @Test
    public void testCostruttore_ComandoInvalido() {
        assertThrows(IllegalArgumentException.class, () -> new Comando(null, "80"));
        assertThrows(IllegalArgumentException.class, () -> new Comando("", "80"));
        assertThrows(IllegalArgumentException.class, () -> new Comando("   ", "80"));
        assertThrows(IllegalArgumentException.class, () -> new Comando("luminosita", null));
    }

    // TC-03: testCostruttoreJson_HappyPath
    @Test
    public void testCostruttoreJson_HappyPath() {
        String jsonValido = "{\"param\":\"temperatura\", \"valore\":\"22.5\"}";
        Comando comando = new Comando(jsonValido);
        
        assertEquals("temperatura", comando.getParam(), "Il parametro estratto dal JSON non è corretto");
        assertEquals("22.5", comando.getValore(), "Il valore estratto dal JSON non è corretto");
    }

    // TC-04: testCostruttoreJson_StringaVuota
    @Test
    public void testCostruttoreJson_StringaVuota() {
        assertThrows(IllegalArgumentException.class, () -> new Comando((String) null), "Deve lanciare eccezione per JSON null");
        assertThrows(IllegalArgumentException.class, () -> new Comando(""), "Deve lanciare eccezione per JSON vuoto");
        assertThrows(IllegalArgumentException.class, () -> new Comando("   "), "Deve lanciare eccezione per JSON blank");
    }

    // TC-05: testCostruttoreJson_JsonMalformato
    @Test
    public void testCostruttoreJson_JsonMalformato() {

        String jsonRotto = "json malformato";
        assertThrows(IllegalArgumentException.class, () -> new Comando(jsonRotto), "Deve lanciare eccezione se la stringa non è un JSON valido");

        // chiavi mancanti
        String jsonSenzaChiavi = "{\"paramSbagliato\":\"luminosita\"}";
        assertThrows(IllegalArgumentException.class, () -> new Comando(jsonSenzaChiavi), "Deve lanciare eccezione se il JSON non ha le chiavi attese");
    }

}