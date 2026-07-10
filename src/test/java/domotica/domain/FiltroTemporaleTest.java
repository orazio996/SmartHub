package domotica.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class FiltroTemporaleTest {

	
	//TC01: testCreazioneValida
    @Test
    public void testCreazioneValida() {
        LocalDate inizio = LocalDate.now().minusWeeks(1);
        LocalDate fine = LocalDate.now();
        FiltroTemporale filtro = new FiltroTemporale(inizio, fine);
        
        assertEquals(inizio, filtro.getDataInizio());
        assertEquals(fine, filtro.getDataFine());
    }

    // TC02: testDataFineNelFuturo
    @Test
    public void testDataFineNelFuturo() {
        LocalDate inizio = LocalDate.now().minusWeeks(1);
        LocalDate fineFutura = LocalDate.now().plusDays(1);
        
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            new FiltroTemporale(inizio, fineFutura); 
        });
        
        assertEquals("La data di fine non può essere nel futuro.", e.getMessage());
    }
    
    // TC03: testDataInizioDopoDataFine
    @Test
    public void testDataInizioDopoDataFine() {
        LocalDate inizio = LocalDate.now();
        LocalDate fine = LocalDate.now().minusWeeks(1);
        
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            new FiltroTemporale(inizio, fine); 
        });
        assertEquals("La data di inizio non può essere dopo la data di fine.", e.getMessage());
    }
}