package domotica.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Evento verificatosi nel sistema in un preciso istante.
 * Contiene l'elenco di tutte le transizioni di stato causate dall'evento.
 */
public class Evento {
    
	
	private String source;
	private long sourceTimestamp;
    private String tipo;
    private long timestamp;
    private String idTarget;
    private List<TransizioneStato> transizioni;

    public Evento(String source, long sourceTimestamp, String tipo, String idTarget, List<TransizioneStato> transizioni) {
        if (tipo == null || idTarget == null || transizioni == null) {
            throw new IllegalArgumentException("I parametri dell'evento non possono essere nulli");
        }
        this.source = source;
        this.sourceTimestamp = sourceTimestamp;
        this.tipo = tipo;
        this.idTarget = idTarget;
        this.timestamp = System.currentTimeMillis();
        this.transizioni = new ArrayList<>(transizioni); 
    }
    
    public Evento(String tipo, String idTarget, List<TransizioneStato> transizioni) {
        if (tipo == null || idTarget == null || transizioni == null) {
            throw new IllegalArgumentException("I parametri dell'evento non possono essere nulli");
        }
        this.source = "system";
        this.sourceTimestamp = System.currentTimeMillis();
        this.tipo = tipo;
        this.idTarget = idTarget;
        this.timestamp = System.currentTimeMillis();
        this.transizioni = new ArrayList<>(transizioni); 
    }
    
    // aggiunto per i test, e caricamento dati da file
    public Evento(String source, long sourceTimestamp, String tipo, String idTarget, List<TransizioneStato> transizioni, long timestamp) {
        if (tipo == null || idTarget == null || transizioni == null) {
            throw new IllegalArgumentException("I parametri dell'evento non possono essere nulli");
        }
        this.source = source;
        this.sourceTimestamp = sourceTimestamp;
        this.tipo = tipo;
        this.idTarget = idTarget;
        this.timestamp = timestamp;
        this.transizioni = new ArrayList<>(transizioni);
    }

    public String getTipo() {
        return tipo;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIdTarget() {
        return idTarget;
    }

    public List<TransizioneStato> getTransizioni() {
        return new ArrayList<>(transizioni);
    }

	public long getSourceTimestamp() {
		return sourceTimestamp;
	}
	
	public String getSource() {
		return source;
	}
}
