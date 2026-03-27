package domotica.domain;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

/**
 * Evento verificatosi nel sistema in un preciso istante.
 * Contiene l'elenco di tutte le transizioni di stato causate dall'evento.
 */
public class Evento {
    
    private String tipo;
    private LocalDateTime timestamp;
    private String idTarget;
    private List<TransizioneStato> transizioni;

    public Evento(String tipo, String idTarget, List<TransizioneStato> transizioni) {
        if (tipo == null || idTarget == null || transizioni == null) {
            throw new IllegalArgumentException("I parametri dell'evento non possono essere nulli");
        }
        this.tipo = tipo;
        this.idTarget = idTarget;
        this.timestamp = LocalDateTime.now();
        this.transizioni = new ArrayList<>(transizioni); 
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getIdTarget() {
        return idTarget;
    }

    public List<TransizioneStato> getTransizioni() {
        return new ArrayList<>(transizioni);
    }
}
