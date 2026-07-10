package domotica.domain;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class TriggerStato extends Trigger {
    
    private Target targetOsservato;
    private String paramOsservato;
    private String operatore;
    private String soglia;
    private static class DatiTriggerStato {
        String paramOsservato;
        String operatore;
        String soglia;
    }

    public TriggerStato(String triggerJson, Target targetOsservato) {
        if (triggerJson == null || targetOsservato == null) {
            throw new IllegalArgumentException("Parametri non validi per TriggerStato");
        }
        
        this.targetOsservato = targetOsservato;
        
        DatiTriggerStato dati;
		try {
			Gson gson = new Gson();
			dati = gson.fromJson(triggerJson, DatiTriggerStato.class);
		} catch (JsonSyntaxException e) {
			throw new IllegalArgumentException("Formato trigger non valido.", e);
		}
        
        if (dati == null || dati.paramOsservato == null || dati.operatore == null || dati.soglia == null) {
            throw new IllegalArgumentException("Il JSON del trigger è incompleto");
        }

        this.paramOsservato = dati.paramOsservato;
        this.operatore = dati.operatore;
        this.soglia = dati.soglia;
    }

    @Override
    public boolean isSoddisfatto(TransizioneStato ts) {
    	
        if (!ts.getIdDispositivo().equals(targetOsservato.getId())) {
            return false;
        }

        if (!ts.getParam().equals(this.paramOsservato)) {
            return false;
        }

        String nuovoValore = ts.getNewVal();
        try {
            switch (operatore) {
                case "=":
                    return nuovoValore.equalsIgnoreCase(soglia);
                case ">":
                    return Double.parseDouble(nuovoValore) > Double.parseDouble(soglia);
                case "<":
                    return Double.parseDouble(nuovoValore) < Double.parseDouble(soglia);
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
}