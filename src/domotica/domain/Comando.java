package domotica.domain;

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