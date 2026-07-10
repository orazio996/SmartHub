package domotica.ui.fx;

import domotica.app.ControllerTargets;
import domotica.app.ControllerMetriche;
import domotica.app.ControllerSequenze; 
import domotica.ui.dto.DispositivoDTO;
import domotica.ui.dto.DescParametroDTO;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import java.util.Map;
import java.util.List;

public class DispositivoController {

    @FXML private Label lblNomeTarget;
    @FXML private Label lblTipoTarget;
    @FXML private VBox contenitoreParametri;

    private ControllerTargets controllerTargets;
    private ControllerSequenze controllerSequenze;
    private DispositivoDTO dispositivoAttuale;
    private ControllerMetriche controllerMetriche;

    // Aggiorna la firma di initDati per ricevere il controller delle sequenze
    public void initDati(DispositivoDTO dispositivo, ControllerTargets controllerTargets, ControllerSequenze controllerSequenze, ControllerMetriche controllerMetriche) {
        this.dispositivoAttuale = dispositivo;
        this.controllerTargets = controllerTargets;
        this.controllerSequenze = controllerSequenze;
        this.controllerMetriche = controllerMetriche;

        lblNomeTarget.setText("Dispositivo: " + dispositivo.id());
        lblTipoTarget.setText("Marca: " + dispositivo.marca() + " | Modello: " + dispositivo.modello());
        contenitoreParametri.getChildren().clear();

        if (dispositivo.parametri() != null) {
            for (Map.Entry<String, DescParametroDTO> entry : dispositivo.parametri().entrySet()) {
                Node rigaParametro = ControlloGraficoFactory.creaControllo(
                    dispositivo.id(), entry.getValue(), 
                    dispositivo.statoAttuale() != null ? dispositivo.statoAttuale().get(entry.getKey()) : null, 
                    controllerTargets
                );
                contenitoreParametri.getChildren().add(rigaParametro);
            }
        }
    }

    @FXML
    private void apriFinestraSequenza() {
        if (controllerSequenze == null || dispositivoAttuale == null) return;

        List<String> sequenzeDisponibili = controllerSequenze.getSequenze();
        if (sequenzeDisponibili == null || sequenzeDisponibili.isEmpty()) {
            mostraAvviso("Nessuna sequenza disponibile nel sistema.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Lancia Sequenza");
        dialog.setHeaderText("Seleziona la sequenza da lanciare su:\n" + dispositivoAttuale.id());

        // Configurazione bottoni del modale
        ButtonType eseguiButtonType = new ButtonType("Lancia", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(eseguiButtonType, ButtonType.CANCEL);

        // Layout interno
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> cmbSequenze = new ComboBox<>();
        cmbSequenze.getItems().addAll(sequenzeDisponibili);
        cmbSequenze.setPromptText("Seleziona...");

        grid.add(new Label("Sequenza:"), 0, 0);
        grid.add(cmbSequenze, 1, 0);

        dialog.getDialogPane().setContent(grid);


        Node eseguiButton = dialog.getDialogPane().lookupButton(eseguiButtonType);
        eseguiButton.setDisable(true);
        cmbSequenze.valueProperty().addListener((observable, oldValue, newValue) -> {
            eseguiButton.setDisable(newValue == null);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == eseguiButtonType) {
                return cmbSequenze.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(idSequenza -> {
            new Thread(() -> {
            	try {
            		controllerSequenze.eseguiSequenza(idSequenza, dispositivoAttuale.id());
                } catch (IllegalArgumentException | IllegalStateException e) {
                    System.err.println("Errore: " + e.getMessage());  
                } catch (Exception e) {
                    System.err.println("Errore critico: " + e.getMessage());
                    e.printStackTrace();
                }
            }).start();
        });
    }
    
    @FXML
    private void apriFinestraReport() {
        if (controllerMetriche == null) return;
        
        String idTargetTarget = dispositivoAttuale.id();
        java.util.Set<String> parametri = dispositivoAttuale.parametri().keySet();

        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/domotica/ui/fx/GeneraReport.fxml"));
            javafx.scene.Parent root = loader.load();
            
            GeneraReportController dialogController = loader.getController();
            dialogController.initDati(idTargetTarget, parametri, controllerMetriche);

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Configurazione Report");
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL); 
            stage.setScene(new javafx.scene.Scene(root));
            stage.showAndWait();
            
        } catch (Exception e) {
            System.err.println("Errore caricamento modale report: " + e.getMessage());
        }
    }

    private void mostraAvviso(String messaggio) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Informazione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}