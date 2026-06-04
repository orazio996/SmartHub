package domotica.services;

import domotica.domain.Comando;

/**
 * Modello per la comunicazione tramite ServizioIPC.
 */
public class RichiestaSH {
    
    private String tipo;
    private String param;
    private String val;

    public RichiestaSH(String tipo, Comando c) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("Il tipo di richiesta non può essere nullo");
        }
        if (c == null) {
            throw new IllegalArgumentException("Il comando non può essere nullo");
        }
        
        this.tipo = tipo;
        this.param = c.getParam();
        this.val = c.getValore();
    }

    public String getTipo() {
        return tipo;
    }

    public String getParam() {
        return param;
    }

    public String getVal() {
        return val;
    }
    
    @Override
    public String toString() {
        return String.format("{\"tipo\":\"%s\", \"param\":\"%s\", \"val\":\"%s\"}", 
                             tipo, param, val);
    }
}