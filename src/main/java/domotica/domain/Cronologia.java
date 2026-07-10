package domotica.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Registro eventi significativi del sistema.
 */
public class Cronologia implements MonitorListener{
    
    private List<Evento> eventi;

    public Cronologia() {
        this.eventi = new ArrayList<>();
    }

    /**
     * Aggiunge un nuovo evento allo storico.
     */
    public void addEvento(Evento e) {
        if (e != null) {
            this.eventi.add(e);
        }
    }

    /**
     * Recupera lo storico completo.
     */
    public List<Evento> getCronologia() {
        return new ArrayList<>(eventi);
    }
    
    public Cronologia getCronologiaObj() {
    	return this;
    }
    
    
    
    /**
     * Recupera gli eventi relativi al target t, all'interno 
     * della finestra di tempo definita dal filtro f.
     */
    public List<Evento> getEventi(Target t, FiltroTemporale f) {
        Objects.requireNonNull(t, "Impossibile filtrare gli eventi: il target non può essere nullo.");
        Objects.requireNonNull(f, "Impossibile filtrare gli eventi: il filtro temporale non può essere nullo.");

        List<Evento> eventi = new ArrayList<>();
        
        long inizioMs = f.getDataInizio().atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant().toEpochMilli();
        long fineMs = f.getDataFine().atTime(23, 59, 59).atZone(java.time.ZoneId.systemDefault())
        		.toInstant().toEpochMilli();
        
        List<String> idValidi = new ArrayList<>();
        for (Dispositivo d : t.getDispositivi()) {
            idValidi.add(d.getId());
        }
        
        for (Evento e : this.eventi) {
        	
            long timestampEvento = e.getTimestamp(); 
            
            boolean dataInclusa = (timestampEvento >= inizioMs && timestampEvento <= fineMs);
            boolean stessoTarget = idValidi.contains(e.getIdTarget());
            
            if (dataInclusa && stessoTarget) {
                eventi.add(e);
            }
        }
        return eventi;
    }
    
    
    public List<Evento> getEventiUltimoComando(){
    	List<Evento> eventiCercati = new ArrayList<Evento>();
    	long sourceTimestamp = 0;
    	for(int i=eventi.size()-1; i>=0; i--) {
    		Evento e = eventi.get(i);
    		if(e.getTipo().equalsIgnoreCase("userCmd")) {
    			if(e.getSourceTimestamp() > sourceTimestamp ) {
    				eventiCercati = new ArrayList<Evento>();
    				eventiCercati.add(e);
    				sourceTimestamp = e.getSourceTimestamp();
    			} else if (e.getSourceTimestamp() == sourceTimestamp) {
    				eventiCercati.add(e);
    			}
    		}
    		if (e.getTimestamp() < sourceTimestamp){ 
				break;
			}
    	}
    	return eventiCercati;
    }
    
    
    /**
     * Svuota la lista, distrugge tutti gli Eventi contenuti.
     */
    public void svuotaCronologia() {
        this.eventi.clear();
    }

    /**
     * Salva gli eventi rilevati dal sistema di monitoraggio.
     */
	@Override
	public void onEvento(Evento e) {
		this.addEvento(e);
	}
	
	public void onErrore(String errore) {
    	// niente per ora
    }
	
}