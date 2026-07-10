package domotica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;

public class ArchivioReportsTest {

    private ArchivioReports archivio;
    private FiltroTemporale filtro;

    @BeforeEach
    public void setup() {
        archivio = new ArchivioReports();
        filtro = new FiltroTemporale(LocalDate.now().minusWeeks(1), LocalDate.now());
    }

    //TC01: testAggiungiReportConNome
    @Test
    public void testAggiungiReportConNome() {
        Report report = new Report(filtro);
        report.setNome("Report Consumi Mensili");
        
        archivio.aggiungiReport(report);
        
        assertEquals(1, archivio.getReports().size());
        assertEquals("Report Consumi Mensili", archivio.getReports().get(0).getNome());
    }

    //TC02: testAggiungiReportSenzaNome
    @Test
    public void testAggiungiReportSenzaNome() {
        Report report1 = new Report(filtro);
        Report report2 = new Report(filtro);
        
        archivio.aggiungiReport(report1);
        archivio.aggiungiReport(report2);
        
        assertEquals(2, archivio.getReports().size());
        assertEquals("Report 1", archivio.getReports().get(0).getNome());
        assertEquals("Report 2", archivio.getReports().get(1).getNome());
        
        assertEquals("Report 1", report1.getNome());
    }

    // TC03: testAggiungiReportNullo
    @Test
    public void testAggiungiReportNullo() {
        NullPointerException eccezione = assertThrows(NullPointerException.class, () -> {
            archivio.aggiungiReport(null);
        });
        
        assertEquals("Impossibile archiviare un report nullo.", eccezione.getMessage());
    }
}