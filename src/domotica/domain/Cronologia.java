package domotica.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Registro eventi significativi del sistema.
 */
public class Cronologia {
    
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
    
    /**
     * Svuota la lista, distrugge tutti gli Eventi contenuti.
     */
    public void svuotaCronologia() {
        this.eventi.clear();
    }
}