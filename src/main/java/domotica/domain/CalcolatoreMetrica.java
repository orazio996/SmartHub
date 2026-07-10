package domotica.domain;

import java.util.List;
import java.util.Objects;

public abstract class CalcolatoreMetrica {
    protected TipoMetrica tipoMetrica;

    
    
    public CalcolatoreMetrica(TipoMetrica tipoMetrica) {
        this.tipoMetrica = Objects.requireNonNull(tipoMetrica, "Il TipoMetrica non può essere nullo.");
    }
    
    

    public Metrica calcolaMetrica(Target target, String param, FiltroTemporale filtro, Cronologia cronologia) {
        
        Objects.requireNonNull(target, "Il target non può essere nullo.");
        Objects.requireNonNull(filtro, "Il filtro temporale non può essere nullo.");
        Objects.requireNonNull(cronologia, "La cronologia non può essere nulla.");

        List<Evento> eventi = cronologia.getEventi(target, filtro);

        double valore = calcola(target, param, eventi, filtro); // serve il filtro di nuovo ??? per log forse

        return new Metrica(valore, getUnitaMisura(), this.tipoMetrica);
    }

    protected abstract double calcola(Target target, String param, List<Evento> eventi, FiltroTemporale filtro);
    protected abstract String getUnitaMisura();
}