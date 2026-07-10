package domotica.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroSequenze {

    private Map<String, SequenzaComandi> registro;

    public RegistroSequenze() {
        this.registro = new HashMap<>();
    }

    public void addSequenza(SequenzaComandi seq) {
        if (seq == null) {
            throw new IllegalArgumentException("Impossibile aggiungere una sequenza nulla al registro.");
        }
        this.registro.put(seq.getId(), seq);
    }

    /**
     * Restituisce la lista di tutti gli ID delle sequenze salvate.
     */
    public List<String> getAllSequenze() {
        return new ArrayList<>(this.registro.keySet());
    }

    /**
     * Recupera una singola sequenza dal suo ID.
     */
    public SequenzaComandi getSequenza(String idSequenza) {
        if (idSequenza == null || idSequenza.isBlank()) {
            throw new IllegalArgumentException("L'ID della sequenza non può essere nullo o vuoto.");
        }
        if (!this.registro.containsKey(idSequenza)) {
            throw new IllegalArgumentException("Nessuna sequenza trovata con ID: " + idSequenza);
        }
        return this.registro.get(idSequenza);
    }
}