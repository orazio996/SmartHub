package domotica.domain;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class TriggerTemporale extends Trigger {
    
    private LocalTime orario;
    private List<DayOfWeek> giorniRipetizione;
    private static class DatiTriggerTemporale {
        String orario;
        List<String> giorniRipetizione;
    }

    public TriggerTemporale(String triggerJson) {
        if (triggerJson == null || triggerJson.isBlank()) {
            throw new IllegalArgumentException("La stringa JSON del trigger temporale non può essere vuota");
        }
        
        DatiTriggerTemporale dati;
		try {
			Gson gson = new Gson();
			dati = gson.fromJson(triggerJson, DatiTriggerTemporale.class);
		} catch (JsonSyntaxException e) {
			throw new IllegalArgumentException("Formato trigger non valido" + e);
		}
        
        if (dati == null || dati.orario == null) {
            throw new IllegalArgumentException("Il JSON del trigger temporale deve contenere 'orarioTarget'");
        }

        try {
            this.orario = LocalTime.parse(dati.orario.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Formato orario non valido nel JSON. Usa HH:mm");
        }

        this.giorniRipetizione = new ArrayList<>();
        if (dati.giorniRipetizione != null) {
            for (String giorno : dati.giorniRipetizione) {
                try {
                    this.giorniRipetizione.add(DayOfWeek.valueOf(giorno.toUpperCase().trim()));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Giorno della settimana non valido nel JSON: " + giorno);
                }
            }
        }
    }

    /**
     * Calcola la distanza in millisecondi tra l'istante attuale e il prossimo giorno/orario valido.
     */
    public long getDelay() {
        LocalDateTime now = LocalDateTime.now();
        
        if (giorniRipetizione == null || giorniRipetizione.isEmpty()) {
            LocalDateTime nextRun = now.toLocalDate().atTime(orario);
            if (now.isAfter(nextRun) || now.isEqual(nextRun)) {
                nextRun = nextRun.plusDays(1);
            }
            return ChronoUnit.MILLIS.between(now, nextRun);
        }

        LocalDateTime candidato = now.toLocalDate().atTime(orario);

        for (int i = 0; i <= 7; i++) {
            DayOfWeek giornoEsaminato = candidato.getDayOfWeek();
            
            if (giorniRipetizione.contains(giornoEsaminato)) {
                
                if (i == 0 && (now.isAfter(candidato) || now.isEqual(candidato))) {
                    candidato = candidato.plusDays(1);
                    continue;
                }

                return ChronoUnit.MILLIS.between(now, candidato);
            }
            candidato = candidato.plusDays(1);
        }
        
        throw new IllegalStateException("Errore nel calcolo del delay temporale: nessun giorno valido trovato.");
    }

    @Override
    public boolean isSoddisfatto(TransizioneStato evento) {
        return false;
    }

    public LocalTime getOrarioTarget() { return orario; }
    public List<DayOfWeek> getGiorniRipetizione() { return giorniRipetizione; }
}