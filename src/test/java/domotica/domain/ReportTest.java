package domotica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class ReportTest {
    
    private FiltroTemporale filtro;
    private Report report;
    
    @BeforeEach
    public void setup() {
    	filtro = new FiltroTemporale(LocalDate.now().minusWeeks(1), LocalDate.now());
    	report = new Report(filtro);
    }

    
    // TC01: testCreazioneValida
    @Test
    public void testCreazioneValida() {
        assertEquals(filtro, report.getFiltroTemporale());
        assertEquals("", report.getNome());
        assertNotNull(report.getMetriche());
        assertEquals(0, report.getMetriche().size());
    }


    // TC02: testCostruttoreFiltroNullo
    @Test
    public void testCostruttoreFiltroNullo() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            new Report(null);
        });
        assertEquals("Il filtro temporale non può essere nullo.", e.getMessage());
    }

    
    // TC03: testSetNomeNullo
    @Test
    public void testSetNomeNullo() {
        
        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            report.setNome(null);
        });
        assertEquals("Il nome del report non può essere nullo.", e.getMessage());
    }

    
    // TC04: testAggiungiMetricaNull
    @Test
    public void testAggiungiMetricaNull() {
        
        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            report.aggiungiMetrica(null);
        });
        assertEquals("Impossibile aggiungere una metrica nulla al report.", e.getMessage());
    }
}