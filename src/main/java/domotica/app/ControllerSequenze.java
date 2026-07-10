package domotica.app;

import java.util.List;
import java.util.Objects;

import domotica.domain.RegistroSequenze;
import domotica.domain.SequenzaComandi;

public class ControllerSequenze {

    private RegistroSequenze registroSequenze;
    private ControllerTargets ct;

    public ControllerSequenze(RegistroSequenze registroSequenze, ControllerTargets ct) {
        this.registroSequenze = Objects.requireNonNull(registroSequenze, "Il RegistroSequenze non può essere nullo");
        this.ct = Objects.requireNonNull(ct, "Il RegistroTargets non può essere nullo");
    }

   
    public List<String> getSequenze() {
        return this.registroSequenze.getAllSequenze();
    }

    /**
     * Esegue una specifica sequenza su un determinato bersaglio.
     */
    public void eseguiSequenza(String idSequenza, String idTarget) throws Exception {
        Objects.requireNonNull(idSequenza, "L'ID della sequenza non può essere nullo");
        Objects.requireNonNull(idTarget, "L'ID del target non può essere nullo");

        SequenzaComandi sequenza = this.registroSequenze.getSequenza(idSequenza);
        ct.eseguiComando(sequenza, idTarget, "user"); 
    }
}