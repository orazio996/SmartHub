package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;

public class CronologiaTest {

    private Cronologia cronologia;
    private Evento evento;

    @BeforeEach
    public void setup() {
        cronologia = new Cronologia();
        evento = new Evento("tipo", "idTarget", new ArrayList<>());
    }

    // TC-01: stato iniziale
    @Test
    public void testStatoIniziale() {
        assertTrue(cronologia.getCronologia().isEmpty(), "La cronologia deve nascere vuota");
    }

    // TC-02: addEvento---> ignora i null 
    @Test
    public void testAddEvento_Valido() {
        cronologia.addEvento(evento);
        assertEquals(1, cronologia.getCronologia().size(), "Deve aggiungere l'evento valido");

        cronologia.addEvento(null);
        assertEquals(1, cronologia.getCronologia().size(), "Deve ignorare i null");
    }

    // TC-03: test copia difensiva
    @Test
    public void testIncapsulamento_CopieDifensive() {
        cronologia.addEvento(evento);
        

        cronologia.getCronologia().clear();
        assertEquals(1, cronologia.getCronologia().size(), "deve restituire una copia difensiva");
    }

    // TC-04: test svuota cronologia
    @Test
    public void testSvuotaCronologia() {
        cronologia.addEvento(evento);
        cronologia.addEvento(new Evento("tipo2", "idTarget2", new ArrayList<>()));
        
        cronologia.svuotaCronologia();
        
        assertTrue(cronologia.getCronologia().isEmpty(), "La cronologia deve essere vuota");
    }
}