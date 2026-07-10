package domotica.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ArchivioReports {
    private List<Report> reports;

    public ArchivioReports() {
        this.reports = new ArrayList<>();
    }

    public void aggiungiReport(Report report) {
        Objects.requireNonNull(report, "Impossibile archiviare un report nullo.");
        if(report.getNome().isBlank()) {
        	report.setNome("Report " + (reports.size() + 1));
        }
        this.reports.add(report);
    }
    
    public List<Report> getReports() { return reports; }
}