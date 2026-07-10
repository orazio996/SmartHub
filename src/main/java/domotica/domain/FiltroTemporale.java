package domotica.domain;

import java.time.LocalDate;
import java.util.Objects;

public class FiltroTemporale {
    private LocalDate dataInizio;
    private LocalDate dataFine;

    public FiltroTemporale(LocalDate dataInizio, LocalDate dataFine) {
        
        this.dataInizio = Objects.requireNonNull(dataInizio, "La data di inizio non può essere nulla.");
        this.dataFine = Objects.requireNonNull(dataFine, "La data di fine non può essere nulla.");

        if (this.dataFine.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("La data di fine non può essere nel futuro.");
        }
        
        if (this.dataInizio.isAfter(this.dataFine)) {
            throw new IllegalArgumentException("La data di inizio non può essere dopo la data di fine.");
        }
    }

    public LocalDate getDataInizio() { return dataInizio; }
    public LocalDate getDataFine() { return dataFine; }
}