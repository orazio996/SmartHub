package domotica.domain;

/**
 * Regista un cambiamento di stato. 
 */
public class TransizioneStato {
    
    private String param;
    private String oldVal;
    private String newVal;
    private String idDispositivo;

    public TransizioneStato(String param, String oldVal, String newVal, String idDispositivo) {
        this.param = param;
        this.oldVal = oldVal;
        this.newVal = newVal;
        this.idDispositivo = idDispositivo;
    }

    public String getParam() {
        return param;
    }

    public String getOldVal() {
        return oldVal;
    }

    public String getNewVal() {
        return newVal;
    }

    public String getIdDispositivo() {
        return idDispositivo;
    }

    @Override
    public String toString() {
        return String.format("Transizione[%s]: %s (%s -> %s)", 
                             idDispositivo, param, oldVal, newVal);
    }
}