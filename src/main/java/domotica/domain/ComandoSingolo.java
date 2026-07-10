package domotica.domain;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

/**
 * Rappresenta un ordine impartito dal sistema (es. "luminosita", "80").
 */
public class ComandoSingolo extends Comando {
    
    private String param;
    private String valore;

    public ComandoSingolo(String param, String valore) {
    	this.param = Objects.requireNonNull(param);
        this.valore = Objects.requireNonNull(valore);
        if (param.isBlank() || valore.isBlank()) {
            throw new IllegalArgumentException("Parametro e valore non possono essere vuoti");
        }
    }
    
    public ComandoSingolo(String cmdJson) {
        if (cmdJson == null || cmdJson.isBlank()) {
            throw new IllegalArgumentException("comando non può essere nulla o vuota.");
        }
        try {
            JsonObject json = JsonParser.parseString(cmdJson).getAsJsonObject();
            this.param = json.get("param").getAsString();
            this.valore = json.get("valore").getAsString();   
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato JSON del comando non valido. Stringa ricevuta: " + cmdJson, e);
        }
    }
    
    
    
    @Override
    public void esegui(Target t, ServizioRete rete, String source, long sourceTimestamp){

    	if(this.getParam().equalsIgnoreCase("wait")) {
    		try {
				Thread.sleep(Long.parseLong(this.valore));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("Valore di wait non numerico" + this.valore, e);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
	            throw new IllegalStateException("Sleep interrotta bruscamente.", e);
			}
    		return;
    	}
        List<Dispositivo> compatibili = t.getDispositiviCompatibili(this);

        for (Dispositivo d : compatibili) {
            RichiestaSH req = new RichiestaSH(t.getId(), source, source + "Cmd", sourceTimestamp, this);
            try {
				rete.send(req, d.getIndirizzo());
			} catch (IOException e) {
				throw new IllegalStateException( "Errore rete, invio a: " + d.getIndirizzo(), e);
			}
        }
    }
    
//    @Override
//    public List<ComandoSingolo> getComandi() {
//    	return List.of(this);
//    }
    

    public String getParam() { return param; }
    public String getValore() { return valore; }
    
    @Override
    public String toString() {
        return "Comando{" + "param='" + param + '\'' + ", valore='" + valore + '\'' + '}';
    }

}