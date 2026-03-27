package domotica.domain;

import java.util.List;

/**
 * Classe Astratta che rappresenta il target di un comando.
 * Può essere un dispositivo singolo o un gruppo.
 * Un gruppo può contenere altri gruppi.
 */
public abstract class Target {

    private String id;

    public Target(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("L'ID del target non può essere nullo");
        }
        this.id = id;
    }

    public String getId() {
        return id;
    }
    
    
    /**
     * Ritorna la lista dei dispositivi fisici all'interno di un gruppo.
     * (Compresi quelli dentro i gruppi interni).
     */
    public abstract List<Dispositivo> getDispositiviCompatibili(Comando c);
}