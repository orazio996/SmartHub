package domotica.domain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rappresenta un dispositivo fisico (es. una lampadina, un termostato).
 */
public class Dispositivo extends Target {

    private String indirizzo;       
    private boolean statoConn; // online/offline
    private DescDispositivo descrizione; // info relative al dispositivo e descrizione del suo stato
    private Map<String, String> stato; // stato operativo del dispositivo

    public Dispositivo(String id, String indirizzo, DescDispositivo descrizione) {
    	
        super(id); 
        this.indirizzo = indirizzo;
        this.statoConn = true;
        this.descrizione = descrizione;
        this.stato = new HashMap<>();
        
        for (String nomeParametro : descrizione.getDescParametri().keySet()) {
            this.stato.put(nomeParametro, "SCONOSCIUTO"); 
        }
    }

    public String getIndirizzo() { return indirizzo; }
    public boolean isOnline() { return statoConn; }
    public void setStatoConn(boolean statoConn) { this.statoConn = statoConn; }
    public DescDispositivo getDescrizione() { return descrizione; }
    public Map<String, String> getStato() { return new HashMap<>(stato); }

    
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
    public List<Dispositivo> getDispositiviCompatibili(Comando c) {
    	
        if (this.descrizione.isCompatibile(c)) {
            return Arrays.asList(this);
        }
        System.out.println("⚠️[Warning] " + this.getId() + ": dispositivo incompatibile!");
        return new ArrayList<>();
    }

    /**
     * Aggiorna lo stato locale del dispositivo
     * e genera una TransizioneStato utile a definire l'evento relativo.
     */
    public TransizioneStato aggiornaStato(String parametro, String nuovoValore) {
        
        if (!this.stato.containsKey(parametro)) {
            throw new IllegalArgumentException("Errore: Il parametro '" + parametro + "' non fa parte di questo dispositivo!");
        }

        String vecchioValore = this.stato.get(parametro);
        this.stato.put(parametro, nuovoValore);
        
        return new TransizioneStato(parametro, vecchioValore, nuovoValore, this.getId());
    }
}