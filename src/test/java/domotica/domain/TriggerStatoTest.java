package domotica.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

class TriggerStatoTest {
	
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

    private Target newTestTarget(String id) {
        return new TargetFinto(id);
    }

    private TransizioneStato newTestTransizione(String idTarget, String parametro, String nuovoValore) {
        return new TransizioneStato(parametro, "vecchioValore", nuovoValore, idTarget); 
    }

    
    // TC01: testCreazioneValida
    @Test
    void testCreazioneValida() {
        Target t = newTestTarget("Termostato_Sala");
        String json = "{\"paramOsservato\": \"temperatura\", \"operatore\": \">\", \"soglia\": \"20\"}";
        
        TriggerStato trigger = new TriggerStato(json, t);
        assertNotNull(trigger);
    }

    
    // TC02: testCreazioneJsonMalformato
    @Test
    void testCreazioneJsonMalformato() {
        Target t = newTestTarget("Termostato_Sala");
        String jsonIncompleto = "{\"paramOsservato\": \"temperatura\"}"; // Manca operatore e soglia
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TriggerStato(jsonIncompleto, t);
        });
    }
    
    
    // TC03: testCreazioneParametriNull
    @Test
    void testCreazioneParametriNulli() {
        Target t = newTestTarget("Termostato_Sala");
        String json = "{\"paramOsservato\": \"temperatura\", \"operatore\": \">\", \"soglia\": \"20\"}";
        
        assertThrows(IllegalArgumentException.class, () -> new TriggerStato(null, t), "Deve rifiutare JSON nullo");
        assertThrows(IllegalArgumentException.class, () -> new TriggerStato(json, null), "Deve rifiutare Target nullo");
    }
    
    
    // TC04: testIsSoddisfattoTargetSbagliato
    @Test
    void testIsSoddisfattoTargetSbagliato() {
        Target t = newTestTarget("Termostato_Sala");
        String json = "{\"paramOsservato\": \"temperatura\", \"operatore\": \">\", \"soglia\": \"20\"}";
        TriggerStato trigger = new TriggerStato(json, t);

        // Arriva un evento dallo STESSO dispositivo, ma cambia un ALTRO parametro
        TransizioneStato evento = newTestTransizione("Termostato_Ufficio", "temperatura", "80");
        
        assertFalse(trigger.isSoddisfatto(evento), "Il trigger deve ignorare le transizioni di target non osservati");
    }
    
    // TC05: testIsSoddisfattoParametroSbagliato
    @Test
    void testIsSoddisfattoParametroSbagliato() {
        Target t = newTestTarget("Termostato_Sala");
        String json = "{\"paramOsservato\": \"temperatura\", \"operatore\": \">\", \"soglia\": \"20\"}";
        TriggerStato trigger = new TriggerStato(json, t);

        TransizioneStato evento = newTestTransizione("Termostato_Sala", "umidita", "80");
        
        assertFalse(trigger.isSoddisfatto(evento), "Il trigger deve ignorare le transizioni di parametri non osservati");
    }

    //TC06: testIsSoddisfattoCondizioneVera
    @Test
    void testIsSoddisfattoCondizioneVera() {
        Target t = newTestTarget("Termostato_Sala");
        String json = "{\"paramOsservato\": \"temperatura\", \"operatore\": \">\", \"soglia\": \"20\"}";
        TriggerStato trigger = new TriggerStato(json, t);

        // Arriva un evento:la temperatura è 22.5
        TransizioneStato evento = newTestTransizione("Termostato_Sala", "temperatura", "22.5");
        
        assertTrue(trigger.isSoddisfatto(evento), "Il trigger dovrebbe scattare perché 22.5 > 20");
    }

    
    // TC07: testIsSoddisfattoCondizioneFalsa
    @Test
    void testIsSoddisfattoCondizioneFalsa() {
        Target t = newTestTarget("Termostato_Sala");
        String json = "{\"paramOsservato\": \"temperatura\", \"operatore\": \">\", \"soglia\": \"20\"}";
        TriggerStato trigger = new TriggerStato(json, t);

        // Arriva un evento: la temperatura è 18
        TransizioneStato evento = newTestTransizione("Termostato_Sala", "temperatura", "18.0");
        
        assertFalse(trigger.isSoddisfatto(evento), "Il trigger non deve scattare perché 18 non è > 20");
    }

    
    // TC08: testIsSoddisfattoOperatoreNonValido
    @Test
    void testIsSoddisfattoOperatoreNonValido() {
        Target t = newTestTarget("Termostato_Sala");
        String json = "{\"paramOsservato\": \"temperatura\", \"operatore\": \"operatoreNonValido\", \"soglia\": \"20\"}";
        TriggerStato trigger = new TriggerStato(json, t);

        TransizioneStato evento = newTestTransizione("Termostato_Sala", "temperatura", "18.0");
        
        assertFalse(trigger.isSoddisfatto(evento), "Il trigger non deve scattare: operatore non valido");
    }
}