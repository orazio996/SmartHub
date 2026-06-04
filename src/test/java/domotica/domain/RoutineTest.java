package domotica.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class RoutineTest {

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

    private Target newTarget() {
        return new TargetFinto("Luce_Salotto");
    }

    private Comando newComando() {
        return new Comando("power", "ON"); 
    }

    private Trigger newTrigger() {
        // Usiamo un trigger temporale reale visto che sappiamo già che funziona!
        String json = "{\"orario\": \"18:00\"}";
        return new TriggerTemporale(json);
    }


    // TC01: testCostruttore
    @Test
    void testCostruttore() {
        Target t = newTarget();
        Comando c = newComando();
        Trigger tr = newTrigger();

        Routine routine = new Routine("Accensione Serali", t, c, tr);

        assertEquals("Accensione Serali", routine.getNome());
        assertEquals(t, routine.getTarget());
        assertEquals(c, routine.getComando());
        assertEquals(tr, routine.getTrigger());
        
        assertTrue(routine.isAbilitata(), "La routine deve nascere abilitata di default");
    }

    
    //TC02: testCostruttoreSenzaNome
    @Test
    void testCostruttoreSenzaNome() {
        Target t = newTarget();
        Comando c = newComando();
        Trigger tr = newTrigger();

        assertThrows(IllegalArgumentException.class, () -> {
            new Routine(null, t, c, tr);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Routine("   ", t, c, tr);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Routine("", t, c, tr);
        });
        
    }

    
    //TC03: testCostruttoreNullParams
    @Test
    void testCostruttoreNullParams() {
        Target t = newTarget();
        Comando c = newComando();
        Trigger tr = newTrigger();

        assertThrows(IllegalArgumentException.class, () -> {
            new Routine("Test Routine", null, c, tr);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Routine("Test Routine", t, null, tr);
        });

        assertThrows(IllegalArgumentException.class, () -> {
            new Routine("Test Routine", t, c, null);
        });
    }
}