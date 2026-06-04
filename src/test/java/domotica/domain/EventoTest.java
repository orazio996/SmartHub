package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

public class EventoTest {

	
	// TC-01: costruttore happy path
    @Test
    public void testCostruttore_HappyPath() {
        List<TransizioneStato> transizioni = new ArrayList<>();
        Evento evento = new Evento("Comando", "CUCINA", transizioni);

        assertEquals("Comando", evento.getTipo());
        assertEquals("CUCINA", evento.getIdTarget());
        assertNotNull(evento.getTimestamp(), "Il timestamp deve essere generato in automatico");
        assertTrue(evento.getTransizioni().isEmpty());
    }

    
    // TC-02: costruttore parametri nulli 
    @Test
    public void testCostruttore_ParametriNulli() {
        List<TransizioneStato> listaVuota = new ArrayList<>();
        
        assertThrows(IllegalArgumentException.class, () -> new Evento(null, "SALA", listaVuota));
        assertThrows(IllegalArgumentException.class, () -> new Evento("Comando", null, listaVuota));
        assertThrows(IllegalArgumentException.class, () -> new Evento("Comando", "SALA", null));
    }

    
    // TC-03: test copia difensiva
    @Test
    public void testImmutabilita_CopieDifensive() {
        List<TransizioneStato> listaEsterna = new ArrayList<>();
        Evento evento = new Evento("Comando", "SALA", listaEsterna);

        listaEsterna.add(null); 

        evento.getTransizioni().add(null);

        assertTrue(evento.getTransizioni().isEmpty(), "L'evento deve essere incapsulato e immutabile");
    }
}