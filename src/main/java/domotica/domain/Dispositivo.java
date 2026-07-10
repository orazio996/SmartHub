package domotica.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Rappresenta un dispositivo fisico (es. una lampadina, un termostato).
 */
public class Dispositivo extends Target {
     
    private String indirizzo;       
    private boolean statoConn; // online/offline
    private long lastSeen;
    private DescDispositivo descrizione; // info relative al dispositivo e descrizione del suo stato
    private Map<String, String> stato; // stato operativo del dispositivo
    private double potenza;

    public Dispositivo(String id, String displayName, String indirizzo, DescDispositivo descrizione) {
    	
        super(id, displayName); 
        this.indirizzo = Objects.requireNonNull(indirizzo);
        this.statoConn = false;
        // nasce offline perchè poi viene messo automaticamente online dal servizio di monitoraggio
        // tramite un PING_SYNC
        this.lastSeen = System.currentTimeMillis();
        this.descrizione = Objects.requireNonNull(descrizione);
        this.stato = new HashMap<>();
        
        for (String nomeParametro : descrizione.getDescParametri().keySet()) {
            this.stato.put(nomeParametro, "SCONOSCIUTO"); 
        }
    }
    
    public Dispositivo(String id, String indirizzo, DescDispositivo descrizione) {
    	
    	super(id); 
    	this.indirizzo = Objects.requireNonNull(indirizzo);
    	this.statoConn = false;
    	this.lastSeen = System.currentTimeMillis();
    	this.descrizione = Objects.requireNonNull(descrizione);
    	this.stato = new HashMap<>();
    	
    	for (String nomeParametro : descrizione.getDescParametri().keySet()) {
    		this.stato.put(nomeParametro, "SCONOSCIUTO"); 
    	}
    }
    
    
    public Dispositivo(String id, String nome, String indirizzo, DescDispositivo descrizione, double potenza) {
    	
        super(id, nome); 
        this.indirizzo = Objects.requireNonNull(indirizzo);
        this.statoConn = false;
        this.lastSeen = System.currentTimeMillis();
        this.descrizione = Objects.requireNonNull(descrizione);
        this.stato = new HashMap<>();
        this.potenza = Objects.requireNonNull(potenza);      
        for (String nomeParametro : descrizione.getDescParametri().keySet()) {
            this.stato.put(nomeParametro, "SCONOSCIUTO"); 
        }
    }

    
    /**
     * Setter e Getter
     */

    public String getIndirizzo() { return indirizzo; }
    public boolean isOnline() { return statoConn; }
    public void setStatoConn(boolean statoConn) { this.statoConn = statoConn; }
    public DescDispositivo getDescrizione() { return descrizione; }
    public void setLastSeen(long timestamp) { this.lastSeen = timestamp; }
    public long getLastSeen() { return this.lastSeen ; }
    public Map<String, String> getStato() { return new HashMap<>(stato); }
    public double getPotenza() { return this.potenza; }
    public void setPotenza(double potenza) { this.potenza = potenza; }

    
    /**
     * Restituiamo una lista contenente il dispositivo.
     * Usa una lista anche per un solo dispositivo per supportare il polimorfismo.
     */
    @Override
    public List<Dispositivo> getDispositivi() {
        return List.of(this);
    }
    
    /**
     * Se il comando è compatibile restituiamo una lista contenente il dispositivo.
     * Altrimenti una lista vuota.
     * Usa una lista anche per un solo dispositivo per supportare il polimorfismo.
     */
    @Override
    public List<Dispositivo> getDispositiviCompatibili(ComandoSingolo c) {
    	
        if (this.isOnline() && this.descrizione.isCompatibile(c)) {
            return Arrays.asList(this);
        }
        System.out.println("[Warning] " + this.getId() + ": dispositivo incompatibile!");
        return new ArrayList<>();
    }

    /**
     * Aggiorna lo stato locale del dispositivo
     * e genera una TransizioneStato utile a definire l'evento relativo.
     */
    public TransizioneStato aggiornaStato(String parametro, String nuovoValore) {
        
        if (!this.stato.containsKey(parametro)) {
            return null;
        }

        String vecchioValore = this.stato.get(parametro);
        this.stato.put(parametro, nuovoValore);
        
        return new TransizioneStato(parametro, vecchioValore, nuovoValore, this.getId());
    }
}