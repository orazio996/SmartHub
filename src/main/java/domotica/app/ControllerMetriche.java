package domotica.app;

import domotica.domain.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ControllerMetriche {
    private ArchivioReports archivioReports;
    private Cronologia cronologia;
    private FactoryMetriche factoryMetriche;
    private RegistroTargets registroTargets; 
    private List<String> tipiMetrica;

    public ControllerMetriche(RegistroTargets registroTargets, 
                              ArchivioReports archivioReports, 
                              Cronologia cronologia, 
                              FactoryMetriche factoryMetriche,
                              List<String> tipiMetrica
                              ) {
        
        this.registroTargets = Objects.requireNonNull(registroTargets);
        this.archivioReports = Objects.requireNonNull(archivioReports);
        this.cronologia = Objects.requireNonNull(cronologia);
        this.factoryMetriche = Objects.requireNonNull(factoryMetriche);
        this.tipiMetrica = tipiMetrica;
    }

    
    public List<String> getTipiMetrica(){
    	return new ArrayList<String>(tipiMetrica);
    }
    public List<Report> getReports(){
    	return new ArrayList<Report>(archivioReports.getReports());
    }
    /**
     * Genera e permette di memorizzare un report.
     */
    public Report generaReport(String idTarget, String param, List<String> metricheSelezionate, LocalDate dataInizio, LocalDate dataFine) {
        
        Objects.requireNonNull(idTarget, "L'ID del target non può essere nullo.");

        Target target = registroTargets.getTarget(idTarget);
        if (target == null) {
            throw new IllegalArgumentException("Target non trovato a sistema per l'ID: " + idTarget);
        }

        FiltroTemporale filtro = new FiltroTemporale(dataInizio, dataFine);
        Report report = new Report(filtro);
        
        for(String metrica : metricheSelezionate) {
        	TipoMetrica tipo = new TipoMetrica(metrica, "Calcolo di " + metrica);
            CalcolatoreMetrica calcolatore = factoryMetriche.creaCalcolatore(tipo);

            Metrica m = calcolatore.calcolaMetrica(target, param, filtro, cronologia);
            
            report.aggiungiMetrica(m);
        }
        return report;
    }
    
    public void salvaReport(Report r, String nomeReport) {
    	if (nomeReport != null && !nomeReport.isBlank()) {
        	r.setNome(nomeReport);
        }
    	archivioReports.aggiungiReport(r);
    }
}