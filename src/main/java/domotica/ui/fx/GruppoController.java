package domotica.ui.fx;

import domotica.app.ControllerTargets;
import domotica.app.ControllerMetriche;
import domotica.app.ControllerSequenze;
import domotica.ui.dto.DispositivoDTO;
import domotica.ui.dto.GruppoDTO;
import domotica.ui.dto.TargetDTO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.util.Pair;
import java.util.List;

public class GruppoController {

    @FXML private Label lblNomeGruppo;
    @FXML private FlowPane grigliaCard;

    private GruppoDTO gruppoAttuale;
    private ControllerTargets controllerTargets;
    private ControllerSequenze controllerSequenze;
    private ControllerMetriche controllerMetriche;
    
    private java.util.Set<String> parametriDisponibili = new java.util.HashSet<>();

    public void initDati(GruppoDTO gruppo, MainController mainController, ControllerTargets controllerTargets, ControllerSequenze controllerSequenze, ControllerMetriche controllerMetriche) {
        this.gruppoAttuale = gruppo;
        this.controllerTargets = controllerTargets;
        this.controllerSequenze = controllerSequenze;
        this.controllerMetriche = controllerMetriche;
        
        lblNomeGruppo.setText("Gruppo: " + gruppo.id());
        grigliaCard.getChildren().clear();
        parametriDisponibili.clear();

        if (gruppo.figli() != null) {
            for (TargetDTO figlio : gruppo.figli()) {
                try {
                    if (figlio instanceof DispositivoDTO) {
                        DispositivoDTO disp = (DispositivoDTO) figlio;
                        if (disp.parametri() != null) {
                            parametriDisponibili.addAll(disp.parametri().keySet());
                        }

                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/domotica/ui/fx/CardDispositivo.fxml"));
                        Parent card = loader.load();
                        CardController cardController = loader.getController();
                        cardController.initDati(disp, controllerTargets);
                        grigliaCard.getChildren().add(card);
                        
                    } else if (figlio instanceof GruppoDTO) {
                        // ... [Codice generazione Card Sotto-Gruppo invariato] ...
                    }
                } catch (Exception e) {
                    System.err.println("[Errore Card Gruppo]: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void apriFinestraSequenza() {
        if (controllerSequenze == null || gruppoAttuale == null) return;
        List<String> sequenze = controllerSequenze.getSequenze();
        if (sequenze == null || sequenze.isEmpty()) {
            mostraAvviso("Nessuna sequenza disponibile.");
            return;
        }

        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Lancia Sequenza su Gruppo");
        dialog.setHeaderText("Seleziona la sequenza da lanciare su: " + gruppoAttuale.id());
        ButtonType eseguiButton = new ButtonType("Lancia", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(eseguiButton, ButtonType.CANCEL);

        ComboBox<String> cmb = new ComboBox<>();
        cmb.getItems().addAll(sequenze);
        
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.add(new Label("Sequenza:"), 0, 0);
        grid.add(cmb, 1, 0);
        dialog.getDialogPane().setContent(grid);

        Node btnLancia = dialog.getDialogPane().lookupButton(eseguiButton);
        btnLancia.setDisable(true);
        cmb.valueProperty().addListener((obs, oldV, newV) -> btnLancia.setDisable(newV == null));

        dialog.setResultConverter(b -> b == eseguiButton ? cmb.getValue() : null);
        dialog.showAndWait().ifPresent(idSeq -> {
            new Thread(() -> {
            	try {
            		controllerSequenze.eseguiSequenza(idSeq, gruppoAttuale.id());
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
    private void apriFinestraComando() {
        if (controllerTargets == null || gruppoAttuale == null) return;

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Comando di Gruppo");
        dialog.setHeaderText("Invia un comando globale a: " + gruppoAttuale.id());

        ButtonType inviaButtonType = new ButtonType("Invia Comando", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(inviaButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> cmbParametro = new ComboBox<>();
        cmbParametro.getItems().addAll(parametriDisponibili);
        cmbParametro.setPromptText("Parametro");

        TextField txtValore = new TextField();
        txtValore.setPromptText("Valore");

        grid.add(new Label("Parametro:"), 0, 0);
        grid.add(cmbParametro, 1, 0);
        grid.add(new Label("Valore:"), 0, 1);
        grid.add(txtValore, 1, 1);

        dialog.getDialogPane().setContent(grid);

        Node inviaBtn = dialog.getDialogPane().lookupButton(inviaButtonType);
        inviaBtn.setDisable(true);
        
        Runnable validaCampi = () -> {
            inviaBtn.setDisable(cmbParametro.getValue() == null || txtValore.getText().trim().isEmpty());
        };
        cmbParametro.valueProperty().addListener((obs, oldV, newV) -> validaCampi.run());
        txtValore.textProperty().addListener((obs, oldV, newV) -> validaCampi.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == inviaButtonType) {
                return new Pair<>(cmbParametro.getValue(), txtValore.getText().trim());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(paramValore -> {
            try {
            	controllerTargets.eseguiComando(paramValore.getKey(), paramValore.getValue(), gruppoAttuale.id(), "user");
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("[Errore]: " + e.getMessage());
            } catch (Exception e) {
    	    	System.err.println("[Errore Critico]: " + e.getMessage());
    	    	e.printStackTrace();
            }
        });
    }
    
    @FXML
    private void apriFinestraReport() {
        if (controllerMetriche == null) return;
        
        String idTargetTarget = gruppoAttuale.id();
        java.util.Set<String> parametri = this.parametriDisponibili; 

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
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Attenzione");
        alert.setHeaderText(null);
        alert.setContentText(messaggio);
        alert.showAndWait();
    }
}