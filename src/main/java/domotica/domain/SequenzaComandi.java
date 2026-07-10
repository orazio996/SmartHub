package domotica.domain;

import java.util.ArrayList;
import java.util.Objects;

import domotica.services.ServizioRete;

public class SequenzaComandi extends Comando{
	
	private String id;
	private String displayName;
	private ArrayList<Comando> comandi;
	
	public SequenzaComandi(String id, String displayName) {
		this.id = Objects.requireNonNull(id);
		this.displayName = Objects.requireNonNull(displayName);
		if(id.isBlank()) {throw new IllegalArgumentException("Id non puo essere vuoto.");}
		comandi = new ArrayList<Comando>();
	}

	@Override
    public void esegui(Target t, ServizioRete rete, String source, long sourceTimestamp){
		if (t == null || rete == null) {
            throw new NullPointerException("Target o Rete non possono essere nulli");
        }
    	for (Comando c : comandi) { 	c.esegui(t, rete, source, sourceTimestamp);	  }
    }
	
	public void addComando(Comando c) {
		if (c == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un comando nullo alla sequenza.");
        }
        // Una sequenza non può aggiungere se stesso --> loop infinito!
		if(c instanceof SequenzaComandi) {
			SequenzaComandi seq = (SequenzaComandi) c;
	        if (!seq.getId().equals(this.getId())) { 
	            this.comandi.add(c);
	        }
		} else {
	        this.comandi.add(c);
	    }
		// if(t.getSequenze().contains(this)) {Impedisci creazione?}
	}
	

	public ArrayList<Comando> getComandi() {
		return comandi;
	}

	public void setComandi(ArrayList<Comando> comandi) {
		this.comandi = comandi;
	}

	public String getId() { return this.id;	}
	public void setDisplayName(String name) { this.displayName = name; }
	public String getDisplayName() { return this.displayName; }
	
}
