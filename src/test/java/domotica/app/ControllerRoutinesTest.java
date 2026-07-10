package domotica.app;

import domotica.domain.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ControllerRoutinesTest {
	
	
	public static Dispositivo newDispositivo() {
		Map<String, DescParametro> descParams = new HashMap<>();
		descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        DescDispositivo desc = new DescDispositivo("Philips Hue", "Lampadina", "Hue White 9W", descParams);
        Dispositivo dispositivo = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", desc);
        return dispositivo;
	}

    private static class TargetFinto extends Target {
        public TargetFinto(String id) {
            super(id);
        }
        @Override
        public List<Dispositivo> getDispositivi() { return null; }
        @Override
        public List<Dispositivo> getDispositiviCompatibili(ComandoSingolo c) { 
        	if(c.getParam().equals("Incompatibile")) {
        		return List.of();
        	}
        return List.of(newDispositivo());
        }
    }

    // conosce solo la "LuceSalotto", "Termosifone" e "SensoreTemperatura".
    private static class RegistroTargetsFinto extends RegistroTargets {
        @Override
        public Target getTarget(String id) {
            if (id.equals("LuceSalotto") || id.equals("Termosifone") || id.equals("SensoreTemperatura")) {
                return new TargetFinto(id);
            }
            return null;
        }
    }


    private RegistroTargetsFinto registroFinto;
    private MotoreRoutine motore;
    private ControllerRoutines controllerRoutines;
    private ControllerTargets controllerTargets;

    @BeforeEach
    void setUp() {
        registroFinto = new RegistroTargetsFinto();
        motore = new MotoreRoutine(controllerTargets);
        controllerRoutines = new ControllerRoutines(motore, registroFinto);
    }

    @AfterEach
    void tearDown() {
        motore.shutdown();
    }


    // TC01: testCostruttore_nullParams
    @Test
    void testCostruttore_nullParams() {
    	assertThrows(NullPointerException.class, () -> new ControllerRoutines(motore,null));
    	assertThrows(NullPointerException.class, () -> new ControllerRoutines(null,registroFinto));
    }
    
    //TC02: testAddRoutine_Time_HappyPath
    @Test
    void testAddRoutine_Time_HappyPath() {
        String cmdJson = "{\"param\": \"power\", \"valore\": \"ON\"}";
        String triggerStr = "{\"orario\": \"08:00\"}"; 

        controllerRoutines.addRoutine("Sveglia", "TIME", "LuceSalotto", cmdJson, triggerStr);

        assertEquals(1, motore.getRoutines().size(), "La routine deve essere registrata nel motore");
        Routine r = motore.getRoutines().get(0);
        
        assertEquals("Sveglia", r.getNome());
        assertEquals("LuceSalotto", r.getTarget().getId());
        assertTrue(r.getTrigger() instanceof TriggerTemporale, "Il trigger deve essere Temporale");
    }

    //TC03: testAddRoutine_Event_HappyPath
    @Test
    void testAddRoutine_Event_HappyPath() {
        String cmdJson = "{\"param\": \"power\", \"valore\": \"ON\"}";
        String triggerStr = "{\"targetOsservato\": \"SensoreTemperatura\", \"paramOsservato\": \"temperatura\", \"operatore\": \"<\", \"soglia\": \"20\"}";

        controllerRoutines.addRoutine("Riscaldamento", "EVENT", "Termosifone", cmdJson, triggerStr);

        assertEquals(1, motore.getRoutines().size());
        Routine r = motore.getRoutines().get(0);
        
        assertEquals("Riscaldamento", r.getNome());
        assertTrue(r.getTrigger() instanceof TriggerStato, "Il trigger deve essere di Stato");
    }

    // TC04: testAddRoutine_TargetNonTrovato
    @Test
    void testAddRoutine_TargetNonTrovato() {
        String cmdJson = "{\"param\": \"power\", \"valore\": \"ON\"}";
        String triggerStr = "{\"orarioTarget\": \"08:00\"}";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
        	controllerRoutines.addRoutine("Test", "TIME", "FornoInesistente", cmdJson, triggerStr);
        });

        assertTrue(exception.getMessage().contains("Target azione non trovato"));
    }

    // TC05: testAddRoutine_TargetOsservatoNonTrovato
    @Test
    void testAddRoutine_TargetOsservatoNonTrovato() {
        String cmdJson = "{\"param\": \"power\", \"valore\": \"ON\"}";
        String triggerStr = "{\"targetOsservato\": \"SensoreInesistente\", \"paramOsservato\": \"temp\", \"operatore\": \">\", \"soglia\": \"20\"}";

        // la luce esiste, ma il sensore nel trigger non esiste
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
        	controllerRoutines.addRoutine("Test", "EVENT", "LuceSalotto", cmdJson, triggerStr);
        });

        assertTrue(exception.getMessage().contains("Target osservato non trovato"));
    }
    
    //TC06: testAddRoutine_TipoSconosciuto
    @Test
    void testAddRoutine_TipoSconosciuto() {
        String cmdJson = "{\"param\": \"power\", \"valore\": \"ON\"}";
        String triggerStr = "{}";

        // Passiamo un tipo inventato
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
        	controllerRoutines.addRoutine("Test", "TIPO_INVENTATO", "LuceSalotto", cmdJson, triggerStr);
        });

        assertTrue(exception.getMessage().contains("Tipo routine sconosciuto"));
    }
    
  //TC07: testAddRoutine_ComandoIncompatibile
    @Test
    void testAddRoutine_ComandoIncompatibile() {
        String cmdJson = "{\"param\": \"Incompatibile\", \"valore\": \"30\"}";
        String triggerStr = "{}";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
        	controllerRoutines.addRoutine("Test", "Event", "LuceSalotto", cmdJson, triggerStr);
        });

        assertTrue(exception.getMessage().contains("non sono compatibili"));
    }

    
    
}