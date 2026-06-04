package domotica.domain;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Rappresenta un ordine impartito dal sistema (es. "luminosita", "80").
 */
public class Comando {
    
    private String param;
    private String valore;

    public Comando(String param, String valore) {
        if (valore == null || param == null || param.trim().isEmpty()) {
            throw new IllegalArgumentException("Parametro e valore non possono essere nulli");
        }
        this.param = param;
        this.valore = valore;
    }
    
    public Comando(String cmdJson) {
        if (cmdJson == null || cmdJson.trim().isEmpty()) {
            throw new IllegalArgumentException("La stringa del comando non può essere nulla o vuota.");
        }
        
        try {
            JsonObject json = JsonParser.parseString(cmdJson).getAsJsonObject();
            
            this.param = json.get("param").getAsString();
            this.valore = json.get("valore").getAsString();
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Formato JSON del comando non valido. Stringa ricevuta: " + cmdJson, e);
        }
    }

    public String getParam() {
        return param;
    }

    public String getValore() {
        return valore;
    }
    
    @Override
    public String toString() {
        return "Comando{" + "param='" + param + '\'' + ", valore='" + valore + '\'' + '}';
    }
}