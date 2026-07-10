package domotica.services;

import java.util.Objects;

import domotica.domain.ComandoSingolo;

/**
 * Modello per la comunicazione tramite ServizioIPC.
 */
public class RichiestaSH {
    
	private String source;
    private String tipo;
    private long sourceTimestamp;
    private String param;
    private String val;
    private String sourceTarget;

    public RichiestaSH(String sourceTarget, String source, String tipo, long sourceTimestamp, ComandoSingolo c) {
    	
    	Objects.requireNonNull(source, "La sorgente non può essere nulla");
        Objects.requireNonNull(tipo, "Il tipo non può essere nullo");
        Objects.requireNonNull(c, "Il comando non può essere nullo");
        Objects.requireNonNull(sourceTarget, "Il comando non può essere nullo");
        
        if (tipo.isBlank()) {
            throw new IllegalArgumentException("Il tipo di richiesta non può essere nullo");
        }
        if (source.isBlank()) {
        	throw new IllegalArgumentException("Il tipo di richiesta non può essere nullo");
        }
        if (sourceTarget.isBlank()) {
        	throw new IllegalArgumentException("Il tipo di richiesta non può essere nullo");
        }
        if(sourceTimestamp <= 0) {
        	throw new IllegalArgumentException("sourceTimestamp non valido");
        }
        
        this.source = source;
        this.tipo = tipo;
        this.sourceTimestamp = sourceTimestamp;
        this.param = c.getParam();
        this.val = c.getValore();
        this.sourceTarget = sourceTarget;
    }

    public String getTipo() {
        return tipo;
    }

    public String getParam() {
        return param;
    }

    public String getVal() {
        return val;
    }
    
    @Override
    public String toString() {
        return String.format("{\"sourceTarget\":\"%s\", \"source\":\"%s\",\"tipo\":\"%s\", \"sourceTimestamp\":\"%d\", \"param\":\"%s\", \"val\":\"%s\"}", 
                             sourceTarget, source, tipo, sourceTimestamp, param, val);
    }
}