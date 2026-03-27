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
    private int porta;       
    private boolean statoConn; // online/offline
    private DescDispositivo descrizione; // info relative al dispositivo e descrizione del suo stato
    private Map<String, String> stato; // stato operativo del dispositivo

    public Dispositivo(String id, String indirizzo, int porta, DescDispositivo descrizione) {
    	
        super(id); 
        this.indirizzo = indirizzo;
        this.porta = porta;
        this.statoConn = true;
        this.descrizione = descrizione;
        this.stato = new HashMap<>();
    }

    public String getIndirizzo() { return indirizzo; }
    public int getPorta() { return porta; }
    public boolean isOnline() { return statoConn; }
    public void setStatoConn(boolean statoConn) { this.statoConn = statoConn; }
    public DescDispositivo getDescrizione() { return descrizione; }
    public Map<String, String> getStato() { return new HashMap<>(stato); }

    
    
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
        return new ArrayList<>();
    }

    /**
     * Aggiorna lo stato locale del dispositivo
     * e genera una TransizioneStato utile a definire l'evento relativo.
     */
    public TransizioneStato aggiornaStato(String parametro, String nuovoValore) {
    	
        String vecchioValore = this.stato.getOrDefault(parametro, "N/A");
        this.stato.put(parametro, nuovoValore);
        
        return new TransizioneStato(parametro, vecchioValore, nuovoValore, this.getId());
    }
}