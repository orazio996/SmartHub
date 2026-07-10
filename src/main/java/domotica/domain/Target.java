package domotica.domain;

import java.util.List;

/**
 * Classe Astratta che rappresenta il target di un comando.
 * Può essere un dispositivo singolo o un gruppo.
 * Un gruppo può contenere altri gruppi.
 */
public abstract class Target {

    
	private String id;
    private String displayName;

    public Target(String id, String displayName) {
        if (id == null || displayName == null || id.isBlank() || displayName.isBlank()) {
            throw new IllegalArgumentException("L'ID del target non può essere nullo");
        }
        this.id = id;
        this.displayName = displayName;
    }

    protected Target(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("L'ID del target non può essere nullo");
        }
        this.id = id;
    }
    
    
    
    public String getId() {
        return id;
    }
    
    public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String nome) {
		this.displayName = nome;
	}
	
	@Override
	public String toString() {
		return "" + displayName + "";
	}

	/**
     * Ritorna la lista dei dispositivi fisici all'interno di un gruppo.
     * (Compresi quelli dentro i gruppi interni).
     */
    public abstract List<Dispositivo> getDispositivi();
    /**
     * Ritorna la lista dei dispositivi compatibili al comando c
     * all'interno di un gruppo.
     * (Compresi quelli dentro i gruppi interni).
     */
    public abstract List<Dispositivo> getDispositiviCompatibili(ComandoSingolo c);
}