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

}