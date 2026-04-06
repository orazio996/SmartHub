package domotica.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Descrive i parametri di stato di uno specifico dispositivo
 * (es. "luminosita", espressa in "%", da "0" a "100").
 */
public class DescParametro {
    
    private String nome;
    private String tipo;        // es. intero, decimale, booleano
    private String unitaMisura; // es. %, °C, Watt
    private boolean readOnly;   // TRUE se è solo un sensore
    private String min;
    private String max;
    private List<String> valoriAccettati; // es. ON, OFF

    public DescParametro(String nome, String tipo, String unitaMisura, boolean readOnly, 
                         String min, String max, List<String> valoriAccettati) {
        this.nome = nome;
        this.tipo = tipo;
        this.unitaMisura = unitaMisura;
        this.readOnly = readOnly;
        this.min = min;
        this.max = max;
        this.valoriAccettati = (valoriAccettati != null) ? new ArrayList<>(valoriAccettati) : new ArrayList<>();
    }

    public String getNome() { return nome; }
    public String getTipo() { return tipo; }
    public String getUnitaMisura() { return unitaMisura; }
    public boolean isReadOnly() { return readOnly; }
    public String getMin() { return min; }
    public String getMax() { return max; }
    
    public List<String> getValoriAccettati() {
        return new ArrayList<>(valoriAccettati);
    }
    
    
    /**
     * Verifica se il valore indicato è valido in relazione al comando specificato.
     */
    public boolean isValoreValido(String valore) {
        // Se è un sensore non si puo cambiare il valore!
        if (this.readOnly) {
            return false;
        }
        // Controllo dei valori accettabili
        if (!this.valoriAccettati.isEmpty()) {
            return this.valoriAccettati.contains(valore);
        }

        // Controllo del range di valori accettabili
        if (this.tipo.equalsIgnoreCase("int") || this.tipo.equalsIgnoreCase("float")) {
            try {
                double valNumerico = Double.parseDouble(valore);
                
                // minimo
                if (this.min != null && !this.min.isEmpty()) {
                    double minimo = Double.parseDouble(this.min);
                    if (valNumerico < minimo) return false;
                }
                
                // massimo
                if (this.max != null && !this.max.isEmpty()) {
                    double massimo = Double.parseDouble(this.max);
                    if (valNumerico > massimo) return false;
                }
            } catch (NumberFormatException e) {
                return false; 
            }
        }

        return true; 
    }
    
}