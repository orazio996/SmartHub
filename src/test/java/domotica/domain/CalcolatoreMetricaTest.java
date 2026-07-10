package domotica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class CalcolatoreMetricaTest {

    class CalcolatoreFinto extends CalcolatoreMetrica {
        public CalcolatoreFinto(TipoMetrica tipoMetrica) {
            super(tipoMetrica);
        }

        @Override
        protected double calcola(Target target, String param, List<Evento> eventi, FiltroTemporale filtro) {
            return 42.0;
        }

        @Override
        protected String getUnitaMisura() {
            return "☺";
        }
    }

    class CronologiaFinta extends Cronologia {
        @Override
        public List<Evento> getEventi(Target t, FiltroTemporale f) {
            return new ArrayList<>();
        }
    }
    

    private TipoMetrica tipoMetrica;
    private CalcolatoreMetrica calcolatore;
    private Target fintoTarget;
    private FiltroTemporale filtro;
    private Cronologia cronologia;

    @BeforeEach
    public void setup() {
    	tipoMetrica = new TipoMetrica("Finta", "Metrica di Test");
        calcolatore = new CalcolatoreFinto(tipoMetrica);
        filtro = new FiltroTemporale(LocalDate.now().minusDays(7), LocalDate.now());
        cronologia = new CronologiaFinta();
        fintoTarget = new Gruppo("target1", false, false);
    }

    
    //TC01: testCalcolaMetrica
    @Test
    public void testCalcolaMetrica() {
        Metrica risultato = calcolatore.calcolaMetrica(fintoTarget, "param", filtro, cronologia);

        assertNotNull(risultato);
        assertEquals(42.0, risultato.getValore(), 0.001); // Assicuriamoci che usi il valore del metodo astratto
        assertEquals("☺", risultato.getUnitaMisura());
        assertEquals(tipoMetrica, risultato.getTipoMetrica());
    }

    
    // TC02: testCalcolaMetricaParametriNulli
    @Test
    public void testCalcolaMetricaParametriNulli() {
        assertThrows(NullPointerException.class, () -> calcolatore.calcolaMetrica(null, "param", filtro, cronologia));
        assertThrows(NullPointerException.class, () -> calcolatore.calcolaMetrica(fintoTarget, "param", null, cronologia));
        assertThrows(NullPointerException.class, () -> calcolatore.calcolaMetrica(fintoTarget, "param", filtro, null));
    }
}