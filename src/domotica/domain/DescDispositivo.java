package domotica.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Descrizione del dispositivo fisico. 
 * Contiene marca, modello e la descrizione dei suoi parametri di stato.
 */
public class DescDispositivo {
    
    private String marca;
    private String tipo;
    private String modello;
    private Map<String, DescParametro> descParametri; 

    public DescDispositivo(String marca, String tipo, String modello, Map<String, DescParametro> descParametri) {
        this.marca = marca;
        this.tipo = tipo;
        this.modello = modello;
        if (descParametri == null || descParametri.isEmpty()) {
            throw new IllegalArgumentException("Un dispositivo deve avere almeno un parametro!");
        }
        this.descParametri = new HashMap<>(descParametri);
    }

    public String getMarca() { return marca; }
    public String getTipo() { return tipo; }
    public String getModello() { return modello; }
    public Map<String, DescParametro> getDescParametri() { return new HashMap<>(descParametri); }

    /**
     * Verifica se il Comando può essere eseguito da questo dispositivo.
     */
    public boolean isCompatibile(Comando c) {
        if (c == null) return false;
        DescParametro descParam = this.descParametri.get(c.getParam());
        if (descParam == null) {
            return false; 
        }
        
        return descParam.isValoreValido(c.getValore());
    }
}