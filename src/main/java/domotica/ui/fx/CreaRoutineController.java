package domotica.ui.fx;

import domotica.app.ControllerRoutines;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Node;

public class CreaRoutineController {

    @FXML private TextField txtNome;
    @FXML private TextField txtIdTarget;
    @FXML private ComboBox<String> cmbTipo;
    
    @FXML private TextField txtCmdParametro;
    @FXML private TextField txtCmdValore;

    @FXML private VBox boxTriggerEvento;
    @FXML private TextField txtTargetOsservato;
    @FXML private TextField txtParamOsservato;
    @FXML private ComboBox<String> cmbOperatore;
    @FXML private TextField txtSoglia;

    @FXML private VBox boxTriggerTempo;
    @FXML private TextField txtOrario;
    @FXML private HBox boxGiorni;

    @FXML private Label lblFeedback;

    private ControllerRoutines controllerRoutines;

    @FXML
    public void initialize() {
        cmbTipo.getItems().addAll("EVENT", "TIME");
        cmbOperatore.getItems().addAll("==", ">", "<", "!=", ">=", "<=");

        // per scambiare i pannelli visibili in base alla scelta
        cmbTipo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boxTriggerEvento.setVisible("EVENT".equals(newVal));
            boxTriggerTempo.setVisible("TIME".equals(newVal));
            
            boxTriggerEvento.setManaged("EVENT".equals(newVal));
            boxTriggerTempo.setManaged("TIME".equals(newVal));
        });
    }

    public void setControllerRoutines(ControllerRoutines controllerRoutines) {
        this.controllerRoutines = controllerRoutines;
    }

    @FXML
    private void salvaRoutine() {
        if (controllerRoutines == null) return;
        
        String nome = txtNome.getText().trim();
        String idTarget = txtIdTarget.getText().trim();
        String tipo = cmbTipo.getValue();
        
        if (nome.isEmpty() || idTarget.isEmpty() || tipo == null || txtCmdParametro.getText().isEmpty()) {
            lblFeedback.setText("Compila tutti i campi generali e comando.");
            return;
        }

        String cmdJson = String.format(
            "{\"param\": \"%s\", \"valore\": \"%s\"}", 
            txtCmdParametro.getText().trim(), 
            txtCmdValore.getText().trim()
        );

        String triggerStr = "";

        if (tipo.equals("EVENT")) {
            triggerStr = String.format(
                "{\"targetOsservato\": \"%s\", \"paramOsservato\": \"%s\", \"operatore\": \"%s\", \"soglia\": \"%s\"}",
                txtTargetOsservato.getText().trim(),
                txtParamOsservato.getText().trim(),
                cmbOperatore.getValue() != null ? cmbOperatore.getValue() : "=", // Adattato all'operatore di default del vecchio codice
                txtSoglia.getText().trim()
            );
            
        } else if (tipo.equals("TIME")) {
            StringBuilder giorniArrayJson = new StringBuilder("[");
            boolean primo = true;
            boolean hasGiorni = false;
            
            for (Node n : boxGiorni.getChildren()) {
                if (n instanceof CheckBox) {
                    CheckBox cb = (CheckBox) n;
                    if (cb.isSelected()) {
                        hasGiorni = true;
                        if (!primo) giorniArrayJson.append(", ");
                        giorniArrayJson.append("\"").append(cb.getUserData().toString()).append("\"");
                        primo = false;
                    }
                }
            }
            giorniArrayJson.append("]");

            if (hasGiorni) {
                triggerStr = String.format(
                    "{\"orario\": \"%s\", \"giorniRipetizione\": %s}", 
                    txtOrario.getText().trim(), 
                    giorniArrayJson.toString()
                );
            } else {
                triggerStr = String.format(
                    "{\"orario\": \"%s\"}", 
                    txtOrario.getText().trim()
                );
            }
        }

        try {
            controllerRoutines.addRoutine(nome, tipo, idTarget, cmdJson, triggerStr);
            lblFeedback.setStyle("-fx-text-fill: green;");
            lblFeedback.setText("Routine creata e attivata con successo!");
            pulisciForm();
        } catch (Exception e) {
            lblFeedback.setStyle("-fx-text-fill: red;");
            lblFeedback.setText("Errore: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void pulisciForm() {
        txtNome.clear();
    }
}