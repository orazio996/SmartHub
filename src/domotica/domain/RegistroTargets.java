package domotica.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contiene tutti i target registrati nel sistema: dispositivi o gruppi.
 */
public class RegistroTargets {

    private Map<String, Target> targets;

    public RegistroTargets() {
        this.targets = new HashMap<>();
    }

    /**
     * Aggiunge un nuovo Target al registro.
     */
    public void addTarget(Target t) {
        if (t != null && t.getId() != null) {
            // se esiste target con stesso ID?
            this.targets.put(t.getId(), t);
        } else {
            throw new IllegalArgumentException("Il Target o il suo ID non possono essere nulli.");
        }
    }

    /**
     * Rimuove un Target dal sistema. 
     * Rimuovere un dispositivo lo cancellerà dal sistema.
     * Rimuovere un gruppo non cancellerà i dispositivi contenuti dal sistema.
     */
    public void removeTarget(String idTarget) {
        if (idTarget != null) {
            this.targets.remove(idTarget);
        }
    }

    /**
     * Recupera un target tramite il suo ID.
     */
    public Target getTarget(String idTarget) {
        if (idTarget == null) {
            return null;
        }
        return this.targets.get(idTarget);
    }

    /**
     * Restituisce tutti i target registrati.
     */
    public List<Target> getAllTargets() {
        return new ArrayList<>(this.targets.values());
    }

    /**
     * Cancella tutti i target.
     */
    public void factoryReset() {
        this.targets.clear();
    }
}
