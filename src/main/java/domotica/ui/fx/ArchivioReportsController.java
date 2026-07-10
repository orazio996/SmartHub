package domotica.ui.fx;

import domotica.app.ControllerMetriche;
// IMPORTA LA TUA CLASSE REPORT QUI
// import domotica.domain.Report; 
import domotica.domain.Report;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import java.util.List;

public class ArchivioReportsController {

    // Sostituisci "Object" con la tua classe "Report"
    @FXML private ListView<Object> listaReports; 
    @FXML private TextArea areaDettaglio;

    private ControllerMetriche controllerMetriche;

    public void initDati(ControllerMetriche controllerMetriche) {
        this.controllerMetriche = controllerMetriche;
        configuraLista();
        caricaReports();
    }

    private void caricaReports() {
        if (controllerMetriche == null) return;
        
        listaReports.getItems().clear();
        
        List<Report> storici = controllerMetriche.getReports(); 
        if (storici != null && !storici.isEmpty()) {
            listaReports.getItems().addAll((List<Report>) storici); 
        } else {
            areaDettaglio.setText("Nessun report presente in archivio.");
        }
    }

    private void configuraLista() {
        listaReports.setCellFactory(param -> new ListCell<Object>() { // Sostituisci Object con Report
            @Override
            protected void updateItem(Object item, boolean empty) { // Sostituisci Object con Report
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString().split("\\n")[0]); 
                }
            }
        });

        listaReports.getSelectionModel().selectedItemProperty().addListener((obs, vecchioValore, nuovoValore) -> {
            if (nuovoValore != null) {
                areaDettaglio.setText(nuovoValore.toString());
            } else {
                areaDettaglio.setText("");
            }
        });
    }
}