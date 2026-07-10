package domotica.ui.fx;

import domotica.app.ControllerMetriche;
import domotica.domain.Report;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GeneraReportController {

    @FXML private Label lblTitolo;
    @FXML private TextField txtNomeReport;
    @FXML private ComboBox<String> cmbParametro;
    @FXML private VBox boxMetriche;
    @FXML private DatePicker dpInizio;
    @FXML private DatePicker dpFine;
    @FXML private Label lblFeedback;
    @FXML private Button btnGenera;

    private String idTarget;
    private ControllerMetriche controllerMetriche;

    @FXML
    public void initialize() {
        dpInizio.setValue(LocalDate.now().minusDays(7));
        dpFine.setValue(LocalDate.now());

        cmbParametro.getItems().add("");
        cmbParametro.valueProperty().addListener((o, oldV, newV) -> validaForm());
        dpInizio.valueProperty().addListener((o, oldV, newV) -> validaForm());
        dpFine.valueProperty().addListener((o, oldV, newV) -> validaForm());
        
        btnGenera.setDisable(true);
    }

    public void initDati(String idTarget, Set<String> parametriDisponibili, ControllerMetriche controllerMetriche) {
        this.idTarget = idTarget;
        this.controllerMetriche = controllerMetriche;
        this.lblTitolo.setText("Genera Report per: " + idTarget);

        this.cmbParametro.getItems().addAll(parametriDisponibili);

        costruisciMetricheDinamiche();
    }

    private void costruisciMetricheDinamiche() {
        boxMetriche.getChildren().clear();

        List<String> tipiMetrica = controllerMetriche.getTipiMetrica();
        
        if (tipiMetrica != null) {
            for (String m : tipiMetrica) {
                CheckBox cb = new CheckBox(m);

                cb.selectedProperty().addListener((o, oldV, newV) -> validaForm());
                boxMetriche.getChildren().add(cb);
            }
        }
        validaForm();
    }

    /**
     * logica di abilitazione del bottone Genera.
     */
    private void validaForm() {
        List<CheckBox> selectedBoxes = boxMetriche.getChildren().stream()
            .filter(node -> node instanceof CheckBox)
            .map(node -> (CheckBox) node)
            .filter(CheckBox::isSelected)
            .collect(Collectors.toList());

        boolean almenoUnaMetrica = !selectedBoxes.isEmpty();

        boolean richiedeParametro = false;
        for (CheckBox cb : selectedBoxes) {
            String testoMetrica = cb.getText().toUpperCase();
            if (testoMetrica.contains("MEDIA") || testoMetrica.contains("MIN") || testoMetrica.contains("MAX")) {
                richiedeParametro = true;
                break;
            }
        }

        boolean parametroInserito = cmbParametro.getValue() != null && !cmbParametro.getValue().trim().isEmpty();

        boolean parametroValido = richiedeParametro ? parametroInserito : true;

        boolean dateValide = dpInizio.getValue() != null &&
                             dpFine.getValue() != null &&
                             !dpInizio.getValue().isAfter(dpFine.getValue());

        btnGenera.setDisable(!(almenoUnaMetrica && parametroValido && dateValide));
    }

    @FXML
    private void eseguiGenerazione() {
        try {
            lblFeedback.setText("Generazione in corso...");
            
            String nomeReportUtente = txtNomeReport.getText().trim();

            String paramSelezionato = cmbParametro.getValue();
            if (paramSelezionato != null && paramSelezionato.trim().isEmpty()) {
                paramSelezionato = null;
            }

            List<String> metricheScelte = boxMetriche.getChildren().stream()
                .filter(node -> node instanceof CheckBox)
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());

            Report reportGenerato = controllerMetriche.generaReport(
                idTarget,
                paramSelezionato,
                metricheScelte,
                dpInizio.getValue(),
                dpFine.getValue()
            );

            mostraDettaglioReport(reportGenerato, nomeReportUtente);

        } catch (Exception e) {
            lblFeedback.setText("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void mostraDettaglioReport(Report report, String nomeReportDaSalvare) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Anteprima Report");
        alert.setHeaderText("Report generato con successo. Vuoi salvarlo nel sistema?");
        
        TextArea areaTesto = new TextArea(report.toString()); 
        areaTesto.setEditable(false);
        areaTesto.setWrapText(true);
        areaTesto.setPrefHeight(250);
        areaTesto.setPrefWidth(400);
        
        GridPane espansione = new GridPane();
        espansione.setMaxWidth(Double.MAX_VALUE);
        espansione.add(areaTesto, 0, 0);
        alert.getDialogPane().setContent(espansione);

        ButtonType btnSalva = new ButtonType("Salva Report", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnScarta = new ButtonType("Scarta", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        alert.getButtonTypes().setAll(btnSalva, btnScarta);

        alert.showAndWait().ifPresent(scelta -> {
            if (scelta == btnSalva) {
                try {
                    controllerMetriche.salvaReport(report, nomeReportDaSalvare);
                    
                    Alert successo = new Alert(Alert.AlertType.INFORMATION);
                    successo.setTitle("Operazione completata");
                    successo.setHeaderText(null);
                    successo.setContentText("Il report è stato salvato correttamente.");
                    successo.showAndWait();
                    
                    chiudiFinestra();
                } catch (Exception e) {
                    Alert errore = new Alert(Alert.AlertType.ERROR, "Salvataggio fallito: " + e.getMessage());
                    errore.showAndWait();
                }
            } else if (scelta == btnScarta) {
                chiudiFinestra();
            }
        });
    }

    @FXML
    private void chiudiFinestra() {
        Stage stage = (Stage) btnGenera.getScene().getWindow();
        stage.close();
    }
}