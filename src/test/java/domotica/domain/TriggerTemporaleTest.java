package domotica.domain;

import org.junit.jupiter.api.Test;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TriggerTemporaleTest {

	
	//TC01: testCreazioneValidaConGiorni
    @Test
    void testCreazioneValidaConGiorni() {
        String json = "{\"orario\": \"18:00\", \"giorniRipetizione\": [\"MONDAY\", \"WEDNESDAY\"]}";
        TriggerTemporale trigger = new TriggerTemporale(json);
        
        assertEquals("18:00", trigger.getOrarioTarget().toString());
        
        List<DayOfWeek> giorni = trigger.getGiorniRipetizione();
        assertTrue(giorni.contains(DayOfWeek.MONDAY));
        assertTrue(giorni.contains(DayOfWeek.WEDNESDAY));
        assertFalse(giorni.contains(DayOfWeek.SUNDAY));
    }

    
    //TC02: testCreazioneOrarioSbagliato
    @Test
    void testCreazioneOrarioSbagliato() {
        String jsonSbagliato = "{\"orario\": \"25:99\"}"; // Orario inesistente
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TriggerTemporale(jsonSbagliato);
        });
    }

    
    // TC03: testCreazioneGiornoSbagliato
    @Test
    void testCreazioneGiornoSbagliato() {
        String jsonSbagliato = "{\"orario\": \"18:00\", \"giorniRipetizione\": [\"LUNEDI\"]}"; // non è in inglese --> non valido
        
        assertThrows(IllegalArgumentException.class, () -> {
            new TriggerTemporale(jsonSbagliato);
        });
    }

    
    // TC04: testGetDelay_Future_noDays
    @Test
    void testGetDelay_Future_noDays() {
        LocalTime targetTime = LocalTime.now().plusHours(1);
        String json = "{\"orario\": \"" + targetTime.withNano(0).toString() + "\", \"giorniRipetizione\": []}";
        TriggerTemporale trigger = new TriggerTemporale(json);

        long delay = trigger.getDelay();
        
        long expectedDelay = 1 * 3600 * 1000L;
        assertEquals(expectedDelay, delay, 2000, "Deve scattare oggi tra 1 ora ");
    }

    // TC05: testGetDelay_Past_noDays
    @Test
    void testGetDelay_Past_noDays() {
        LocalTime targetTime = LocalTime.now().minusHours(1);
        String json = "{\"orario\": \"" + targetTime.withNano(0).toString() + "\", \"giorniRipetizione\": []}";
        TriggerTemporale trigger = new TriggerTemporale(json);
        
        long delay = trigger.getDelay();
        
        long expectedDelay = 23 * 3600 * 1000L;
        assertEquals(expectedDelay, delay, 2000, "Deve scattare tra 23 ore");
    }



    // TC06: testGetDelay_Future_withDays_todayInList
    @Test
    void testGetDelay_Future_withDays_todayInList() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime targetTime = now.toLocalTime().plusHours(1);
        String oggi = now.getDayOfWeek().name();
        
        String json = "{\"orario\": \"" + targetTime.withNano(0).toString() + "\", \"giorniRipetizione\": [\"" + oggi + "\"]}";
        TriggerTemporale trigger = new TriggerTemporale(json);
        
        long delay = trigger.getDelay();
        long expectedDelay = 1 * 3600 * 1000L;
        assertEquals(expectedDelay, delay, 2000, "Deve scattare oggi tra 1 ora");
    }

    
    // TC07: testGetDelay_Past_withDays_todayInList
    @Test
    void testGetDelay_Past_withDays_todayInList() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime targetTime = now.toLocalTime().minusHours(1);
        String oggi = now.getDayOfWeek().name();
        String domani = now.plusDays(1).getDayOfWeek().name();
        
        String json = "{\"orario\": \"" + targetTime.withNano(0).toString() + "\", \"giorniRipetizione\": [\"" + oggi + "\", \"" + domani + "\"]}";
        TriggerTemporale trigger = new TriggerTemporale(json);
        
        long delay = trigger.getDelay();
        
        long expectedDelay = 23 * 3600 * 1000L;
        assertEquals(expectedDelay, delay, 2000, "Deve saltare a domani ignorando l'orario ormai è passato per oggi");
    }

    // TC08: testGetDelay_withDays_todayNotList
    @Test
    void testGetDelay_withDays_todayNotList() {
        LocalDateTime now = LocalDateTime.now();
        LocalTime targetTime = now.toLocalTime();
        String dopodomani = now.plusDays(2).getDayOfWeek().name();
        
        String json = "{\"orario\": \"" + targetTime.withNano(0).toString() + "\", \"giorniRipetizione\": [\"" + dopodomani + "\"]}";
        TriggerTemporale trigger = new TriggerTemporale(json);
        
        long delay = trigger.getDelay();

        long expectedDelay = 48 * 3600 * 1000L;
        assertEquals(expectedDelay, delay, 2000, "Deve saltare direttamente a dopodomani");
    }

}