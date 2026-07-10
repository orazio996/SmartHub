package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RegistroSequenzeTest {

    private RegistroSequenze registro;
    private SequenzaComandi seq1;
    private SequenzaComandi seq2;

    @BeforeEach
    public void setUp() {
        registro = new RegistroSequenze();
        seq1 = new SequenzaComandi("SEQ-01", "Macro Luci Mattina");
        seq2 = new SequenzaComandi("SEQ-02", "Macro Spegni Tutto");
    }


    // TC-01: testAddSequenza_HappyPath
    @Test
    public void testAddSequenza_HappyPath() {
        assertDoesNotThrow(() -> registro.addSequenza(seq1));
        
        SequenzaComandi recuperata = registro.getSequenza("SEQ-01");
        assertEquals(seq1, recuperata, "La sequenza recuperata non coincide con quella inserita");
    }


    // TC-02: testAddSequenza_Null
    @Test
    public void testAddSequenza_Null() {
        IllegalArgumentException eccezione = assertThrows(
            IllegalArgumentException.class, 
            () -> registro.addSequenza(null)
        );
        assertEquals("Impossibile aggiungere una sequenza nulla al registro.", eccezione.getMessage());
    }


    // TC-03: testGetSequenza_HappyPath
    @Test
    public void testGetSequenza_HappyPath() {
        registro.addSequenza(seq1);
        registro.addSequenza(seq2);

        SequenzaComandi r1 = registro.getSequenza("SEQ-01");
        SequenzaComandi r2 = registro.getSequenza("SEQ-02");

        assertEquals("SEQ-01", r1.getId());
        assertEquals("SEQ-02", r2.getId());
    }


    // TC-04: testGetSequenza_NonEsistente
    @Test
    public void testGetSequenza_NonEsistente() {
        registro.addSequenza(seq1);

        IllegalArgumentException eccezione = assertThrows(
            IllegalArgumentException.class, 
            () -> registro.getSequenza("ID-INESISTENTE")
        );
        assertEquals("Nessuna sequenza trovata con ID: ID-INESISTENTE", eccezione.getMessage());
    }


    // TC-05: testGetSequenza_ParametriInvalidi
    @Test
    public void testGetSequenza_ParametriInvalidi() {
        assertThrows(IllegalArgumentException.class, () -> registro.getSequenza(null));
        assertThrows(IllegalArgumentException.class, () -> registro.getSequenza(""));
        assertThrows(IllegalArgumentException.class, () -> registro.getSequenza("   "));
    }


    // TC-06: testGetAllSequenze
    @Test
    public void testGetAllSequenze() {
        List<String> chiaviVuote = registro.getAllSequenze();
        assertTrue(chiaviVuote.isEmpty(), "Il registro dovrebbe restituire una lista vuota");

        registro.addSequenza(seq1);
        registro.addSequenza(seq2);

        List<String> chiavi = registro.getAllSequenze();

        assertEquals(2, chiavi.size(), "La lista dovrebbe contenere esattamente 2 ID");
        assertTrue(chiavi.contains("SEQ-01"), "La lista non contiene l'ID della prima sequenza");
        assertTrue(chiavi.contains("SEQ-02"), "La lista non contiene l'ID della seconda sequenza");
    }
}