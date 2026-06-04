package domotica.domain;

import domotica.app.ControllerTargets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class MotoreRoutineTest {

    // Estendiamo il controller vero per intercettare i comandi senza mandarli veramente
    private static class ControllerTargetsFinto extends ControllerTargets {
        boolean isComandoEseguito = false;
        String paramRicevuto = null;
        String valoreRicevuto = null;
        String targetRicevuto = null;

        public ControllerTargetsFinto() { 
            super(null, null); 
        }

        @Override
        public void eseguiComando(String param, String valore, String idTarget) {
            this.isComandoEseguito = true;
            this.paramRicevuto = param;
            this.valoreRicevuto = valore;
            this.targetRicevuto = idTarget;
        }
    }

    // questo lo estendiamo per aver un target generico
    private static class TargetFinto extends Target {
        public TargetFinto(String id) {
            super(id);
        }
		@Override
		public List<Dispositivo> getDispositivi() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public List<Dispositivo> getDispositiviCompatibili(Comando c) {
			// TODO Auto-generated method stub
			return null;
		}
    }

    private ControllerTargetsFinto controllerFinto;
    private MotoreRoutine motore;

    @BeforeEach
    void setUp() {
    	controllerFinto = new ControllerTargetsFinto();
        motore = new MotoreRoutine(controllerFinto);
    }

    @AfterEach
    void tearDown() {
        motore.shutdown(); 
    }

    private Evento creaEvento(String parametro, String nuovoValore, String target) {
        return new Evento("CAMBIO_STATO", target, List.of(new TransizioneStato(parametro, "vecchio", nuovoValore, target)));
    }

    
    // TC01: testAddRoutine
    @Test
    void testAddRoutine() {
        Target t = new TargetFinto("Termostato");
        Target tOss = new TargetFinto("SensoreTemperatura");
        Comando c = new Comando("power", "ON");
        Trigger tr = new TriggerStato("{\"paramOsservato\": \"temperatura\", \"operatore\": \"<\", \"soglia\": \"18\"}", tOss);
        
        Routine r = new Routine("Riscaldamento", t, c, tr);
        motore.addRoutine(r);
        
        assertEquals(1, motore.getRoutines().size(), "La routine deve essere aggiunta alla lista");
    }

    
    //TC02: testOnEvento_HappyPath
    @Test
    void testOnEvento_HappyPath() {
        Target t = new TargetFinto("Termostato");
        Target tOss = new TargetFinto("SensoreTemperatura");
        Comando c = new Comando("power", "ON");
        Trigger tr = new TriggerStato("{\"paramOsservato\": \"temperatura\", \"operatore\": \"<\", \"soglia\": \"18\"}", tOss);
        
        Routine r = new Routine("Riscaldamento", t, c, tr); 
        motore.addRoutine(r);

        //evento su tOss
        Evento evento = creaEvento("temperatura", "16.0", "SensoreTemperatura");
        motore.onEvento(evento);

        assertTrue(controllerFinto.isComandoEseguito, "Il motore doveva far scattare il controller!");
        assertEquals("power", controllerFinto.paramRicevuto);
        assertEquals("ON", controllerFinto.valoreRicevuto);
        assertEquals("Termostato", controllerFinto.targetRicevuto);
    }

    
    // TC03: testOnEvento_CondizioneNonSoddisfatta
    @Test
    void testOnEvento_CondizioneNonSoddisfatta() {
    	Target t = new TargetFinto("Termostato");
        Target tOss = new TargetFinto("SensoreTemperatura");
        Comando c = new Comando("power", "ON");
        Trigger tr = new TriggerStato("{\"paramOsservato\": \"temperatura\", \"operatore\": \"<\", \"soglia\": \"18\"}", tOss);
        
        Routine r = new Routine("Riscaldamento", t, c, tr); 
        motore.addRoutine(r);

        // evento che non deve attivare la routine
        Evento evento = creaEvento("temperatura", "20.0", "SensoreTemperatura");
        motore.onEvento(evento);

        assertFalse(controllerFinto.isComandoEseguito, "La routine non deve essere attivata.");
    }

    
    // TC04: testOnEvento_RoutineDisabilitata
    @Test
    void testOnEvento_RoutineDisabilitata() {
    	Target t = new TargetFinto("Termostato");
        Target tOss = new TargetFinto("SensoreTemperatura");
        Comando c = new Comando("power", "ON");
        Trigger tr = new TriggerStato("{\"paramOsservato\": \"temperatura\", \"operatore\": \"<\", \"soglia\": \"18\"}", tOss);
        
        Routine r = new Routine("Riscaldamento", t, c, tr); 
        r.setAbilitata(false); 
        motore.addRoutine(r);


        // condizione vera ma routine spenta
        Evento evento = creaEvento("temperatura", "10.0", "SensoreTemperatura");
        motore.onEvento(evento);

        assertFalse(controllerFinto.isComandoEseguito, "La routine é disabilitata: non deve attivarsi.");
    }
    
    

    // sovrascrive getDelay() per restituire sempre 50ms
    private static class TriggerTemporaleFinto extends TriggerTemporale {
        public TriggerTemporaleFinto() {
            super("{\"orario\": \"12:00:00\", \"giorniRipetizione\": []}"); 
        }

        @Override
        public long getDelay() {
            return 50L; // Il timer scatterà dopo soli 50 millisecondi
        }
    }

    
    // TC05: testSchedule_RoutineTemporaleAbilitata
    @Test
    void testSchedule_RoutineTemporale_HappyPath() throws InterruptedException {
        Target t = new TargetFinto("Irrigatore");
        Comando c = new Comando("power", "ON");
        TriggerTemporaleFinto tr = new TriggerTemporaleFinto();
        
        Routine r = new Routine("Irrigazione", t, c, tr);
        
        motore.addRoutine(r);

        // per dare tempo al timer di scattare
        Thread.sleep(150); 

        assertTrue(controllerFinto.isComandoEseguito, "Lo scheduler doveva eseguire il comando");
        assertEquals("power", controllerFinto.paramRicevuto);
        assertEquals("ON", controllerFinto.valoreRicevuto);
        assertEquals("Irrigatore", controllerFinto.targetRicevuto);
    }


    // TC06: testSchedule_RoutineTemporaleDisabilitata
    @Test
    void testSchedule_RoutineTemporaleDisabilitata() throws InterruptedException {
        Target t = new TargetFinto("Irrigatore");
        Comando c = new Comando("power", "ON");
        TriggerTemporaleFinto tr = new TriggerTemporaleFinto();
        
        Routine r = new Routine("Irrigazione", t, c, tr);
        r.setAbilitata(false); 
        
        motore.addRoutine(r);
        Thread.sleep(150);

        assertFalse(controllerFinto.isComandoEseguito, "La routine temporale è disabilitata, non deve attivarsi.");
    }
}