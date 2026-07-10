package domotica.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Report {
    private String nome;
    private FiltroTemporale filtro;
    private List<Metrica> metriche;

    public Report(FiltroTemporale filtro) {
        this.filtro = Objects.requireNonNull(filtro, "Il filtro temporale non può essere nullo.");
        this.metriche = new ArrayList<>();
        this.nome = "";
    }

    public void setNome(String nome) {
        Objects.requireNonNull(nome, "Il nome del report non può essere nullo.");
        this.nome = nome;
    }
    
    public void aggiungiMetrica(Metrica metrica) {
        Objects.requireNonNull(metrica, "Impossibile aggiungere una metrica nulla al report.");
        this.metriche.add(metrica);
    }

    public String getNome() { return nome; }
    public FiltroTemporale getFiltroTemporale() { return filtro; }
    public List<Metrica> getMetriche() { return metriche; }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        
        sb.append("Nome Report: ").append(this.nome).append("\n");
        sb.append("Range temporale: ")
          .append(this.filtro.getDataInizio())
          .append(" / ")
          .append(this.filtro.getDataFine())
          .append("\n");
        
        sb.append("Metriche:\n");
        if (this.metriche != null) {
            for (Metrica m : this.metriche) {
                sb.append("- ")
                  .append(m.getTipoMetrica())
                  .append(": ")
                  .append(String.format("%.2f", m.getValore())) // Formattazione a 2 cifre decimali
                  .append(" ") // Separato per non innescare la concatenazione col '+'
                  .append(m.getUnitaMisura())
                  .append("\n");
            }
        }
        
        return sb.toString();
    }
}