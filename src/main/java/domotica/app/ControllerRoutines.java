package domotica.app;

import domotica.domain.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Objects;

public class ControllerRoutines {

    private MotoreRoutine motoreRoutine;
    private RegistroTargets registroTargets;

    public ControllerRoutines(MotoreRoutine motoreRoutine, RegistroTargets registroTargets) {
        this.motoreRoutine = Objects.requireNonNull(motoreRoutine); 
        this.registroTargets = Objects.requireNonNull(registroTargets);
    }

    public void addRoutine(String nome, String tipo, String idTarget, String cmd, String triggerStr) {
        
        Target t = registroTargets.getTarget(idTarget);
        if (t == null) {
            throw new IllegalArgumentException("Target azione non trovato: " + idTarget);
        }
        ComandoSingolo c = new ComandoSingolo(cmd); 
        if (t.getDispositiviCompatibili(c).isEmpty()) {
        	throw new IllegalArgumentException("Comando e target non sono compatibili");
        }
        Trigger tr = null;


        if (tipo.equalsIgnoreCase("TIME")) {
        	
            tr = new TriggerTemporale(triggerStr); 
            
        } else if (tipo.equalsIgnoreCase("EVENT")) {
            
            String idTargetOsservato;
			try {
				JsonObject json = JsonParser.parseString(triggerStr).getAsJsonObject();
				idTargetOsservato = json.get("targetOsservato").getAsString();
			} catch (JsonSyntaxException | IllegalArgumentException e) {
				throw new IllegalArgumentException("Formato trigger non valido", e);
			}

            Target tOss = registroTargets.getTarget(idTargetOsservato);
            if (tOss == null) {
                throw new IllegalArgumentException("Target osservato non trovato: " + idTargetOsservato);
            }

            tr = new TriggerStato(triggerStr, tOss);
            
        } else {
            throw new IllegalArgumentException("Tipo routine sconosciuto: " + tipo);
        }

        Routine r = new Routine(nome, t, c, tr);
        motoreRoutine.addRoutine(r);
        
        System.out.println("[CONTROLLER_ROUTINES] Routine " + nome + " creata con successo");
    }
}