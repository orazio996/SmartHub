package domotica.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un raggruppamento di Dispositivi (es. "Luci Salotto", "Piano Terra").
 * Può contenere Dispositivi o altri Gruppi.
 */
public class Gruppo extends Target {

    private boolean isPredef; // predefinito --> non cancellabile
    private boolean isStanza; // non vogliamo che si scelga 'riscaldamento' come posizione di un dispositivo
    private List<Target> figli;

    public Gruppo(String id, boolean isPredef, boolean isStanza) {
        super(id);
        this.isPredef = isPredef;
        this.isStanza = isStanza;
        this.figli = new ArrayList<>();
    }


    public boolean isPredef() { return isPredef; }
    public boolean isStanza() { return isStanza; }
    public List<Target> getFigli() { return new ArrayList<>(figli); }

    public void addTarget(Target t) {
        // Un gruppo non può aggiungere se stesso --> loop infinito!
        if (t != null && !t.getId().equals(this.getId())) { 
            this.figli.add(t);
        }
    }

    public void removeTarget(Target t) {
        if (t != null) {
            this.figli.remove(t);
        }
    }

    
    
    /**
     * Restituisce una lista di dispositivi compatibili con il comando impartito.
     * Utilizza la ricorsione per restituire anche i figli dei gruppi innestati.
     * Se il figlio è un Dispositivo singolo, mi darà se stesso (se compatibile).
     * Se il figlio è un altro Gruppo si attiva la ricorsione!
     */
    @Override
    public List<Dispositivo> getDispositiviCompatibili(Comando c) {
        List<Dispositivo> compatibili = new ArrayList<>();
        
        for (Target figlio : figli) {
            compatibili.addAll(figlio.getDispositiviCompatibili(c));
        }
        
        return compatibili;
    }
}