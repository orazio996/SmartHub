package domotica.domain;

import java.util.Objects;

public class Metrica {
	
    private double valore;
    private String unitaMisura;
    private TipoMetrica tipoMetrica;

    public Metrica(double valore, String unitaMisura, TipoMetrica tipoMetrica) {
        
        if (Double.isNaN(valore)) {
            throw new IllegalArgumentException("Il valore della metrica non può essere NaN");
        }
        
        this.unitaMisura = Objects.requireNonNull(unitaMisura, "L'unità di misura non può essere nulla.");
        if (this.unitaMisura.trim().isEmpty()) {
            throw new IllegalArgumentException("L'unità di misura non può essere vuota.");
        }

        this.tipoMetrica = Objects.requireNonNull(tipoMetrica, "Il TipoMetrica non può essere nullo.");
        this.valore = valore;
    }

    public double getValore() { return valore; }
    public String getUnitaMisura() { return unitaMisura; }
    public TipoMetrica getTipoMetrica() { return tipoMetrica; }
}