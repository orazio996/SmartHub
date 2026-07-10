package domotica.domain;

import java.util.Objects;

public class FactoryMetriche {

    /**
     * Crea e restituisce il calcolatore specifico in base al TipoMetrica richiesto.
     */
    public CalcolatoreMetrica creaCalcolatore(TipoMetrica tipoMetrica) {

        Objects.requireNonNull(tipoMetrica, "Impossibile creare un calcolatore per un TipoMetrica nullo.");
        
        String nomeMetrica = tipoMetrica.getNome();
        
        if ("Consumo".equalsIgnoreCase(nomeMetrica)) {
            return new CalcolatoreConsumo(tipoMetrica);
        } else if ("Uptime".equalsIgnoreCase(nomeMetrica)) {
        	return new CalcolatoreUptime(tipoMetrica);
        } else if ("min".equalsIgnoreCase(nomeMetrica) || "Max".equalsIgnoreCase(nomeMetrica)) {
        	return new CalcolatoreMinMax(tipoMetrica);
        }
        
        //qua aggiungeremo altre metriche 
        
        throw new IllegalArgumentException("Nessun calcolatore supportato per la metrica: " + nomeMetrica);
    }
}