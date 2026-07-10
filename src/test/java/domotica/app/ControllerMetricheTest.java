package domotica.app;

import domotica.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

public class ControllerMetricheTest {

    private ControllerMetriche controller;
    private ArchivioReports archivio;
    private Cronologia cronologia;
    private FactoryMetriche factory;
    RegistroTargets registro;
    
    
    
    /**
     * Stub del registroTarget che ritorna al massimo il target specificato
     */
    class RegistroTargetsTest extends RegistroTargets {
        @Override
        public Target getTarget(String id) {
            if ("target1".equals(id)) {
                return new Gruppo("target1", false, false);
            }
            return null;
        }
    }

    
    /**
     * crea un evento di tipo tipo che cambia il parametro 'power' da prima a dopo
     */
    public Evento newEvento(String tipo, String prima, String dopo, long timestamp) {
    	return new Evento("source", timestamp, tipo, "target1", List.of(new TransizioneStato("power", prima, dopo, "target1")), timestamp);
    }


    @BeforeEach
    public void setup() {
        archivio = new ArchivioReports();
        cronologia = new Cronologia();
        factory = new FactoryMetriche();
        registro = new RegistroTargetsTest();

        controller = new ControllerMetriche(registro, archivio, cronologia, factory, List.of("Consumo", "Uptime", "Media", "min", "Max", "n_Disconn"));
    }

    // TC01 testGeneraSalvaReport
    @Test
    public void testGeneraSalvaReport() {
    	
    	long ora = System.currentTimeMillis();
    	long giornoMs = 86400000L; // millisecondi in 24 ore

    	Evento eventoOn = newEvento("userCmd", "OFF", "ON", ora - (6 * giornoMs));
    	Evento eventoDisconn = newEvento("DISCONNESSIONE", "ON", "ON", ora - (5 * giornoMs));
    	Evento eventoConn = newEvento("CONNESSIONE", "ON", "ON", ora - (4 * giornoMs));
    	Evento eventoOff = newEvento("userCmd", "ON", "OFF", ora - (1 * giornoMs));
        cronologia.addEvento(eventoOn);
        cronologia.addEvento(eventoDisconn);
        cronologia.addEvento(eventoConn);
        cronologia.addEvento(eventoOff);

        LocalDate inizio = LocalDate.now().minusDays(7);
        LocalDate fine = LocalDate.now();
        
        Report report = controller.generaReport("target1", "param", List.of("Consumo","Uptime"), inizio, fine);
        assertNotNull(report);
        controller.salvaReport(report, "");
        assertEquals("Report 1", report.getNome());
        controller.salvaReport(report, "reportTest");
        assertEquals("reportTest", report.getNome());

        assertEquals(2, archivio.getReports().size());
        assertEquals(report, archivio.getReports().get(0));

        assertEquals(2, report.getMetriche().size());
    }
    

    // TC02: testTargetNonTrovato
    @Test
    public void testTargetNonTrovato() {
        
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            controller.generaReport("idFalso","param", List.of("Consumo"), LocalDate.now(), LocalDate.now());
        });
        
        assertTrue(e.getMessage().contains("Target non trovato"));
        assertEquals(0, archivio.getReports().size());
    }

    
    // TC03: testDateInvertite
    @Test
    public void testDateInvertite() {
        LocalDate inizio = LocalDate.now();
        LocalDate fine = LocalDate.now().minusDays(3);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            controller.generaReport("target1","param", List.of("Consumo"), inizio, fine);
        });
        
        assertTrue(e.getMessage().contains("dopo la data di fine"));
    }

    
    //TC04: testMetricaInesistente
    @Test
    public void testMetricaInesistente() {
    	LocalDate inizio = LocalDate.now().minusDays(7);
        LocalDate fine = LocalDate.now();
        
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            controller.generaReport("target1", "param", List.of("Metrica inventata"), inizio, fine);
        });
        
        assertTrue(e.getMessage().contains("Nessun calcolatore supportato"));
    }
}