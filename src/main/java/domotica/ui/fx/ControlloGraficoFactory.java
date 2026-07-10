package domotica.ui.fx;

import domotica.app.ControllerTargets;
import domotica.ui.dto.DescParametroDTO;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ControlloGraficoFactory {

    public static Node creaControllo(String idDispositivo, DescParametroDTO param, String valoreCorrente, ControllerTargets controllerTargets) {
        String labelText = param.nome();
        if (param.unitaMisura() != null && !param.unitaMisura().isEmpty()) {
            labelText += " (" + param.unitaMisura() + ")";
        }
        Label labelNome = new Label(labelText);
        labelNome.setPrefWidth(150);
        labelNome.setWrapText(true);
        labelNome.setStyle("-fx-text-fill: #333333;");

        Node controlloGrafico;

        if (param.valoriAccettati() != null && !param.valoriAccettati().isEmpty()) {
            ComboBox<String> combo = new ComboBox<>();
            combo.getItems().addAll(param.valoriAccettati());
            if (valoreCorrente != null) combo.setValue(valoreCorrente);

            combo.setOnAction(e -> eseguiComando(controllerTargets, param.nome(), combo.getValue(), idDispositivo));
            controlloGrafico = combo;

        } else if (param.min() != null && !param.min().isEmpty() && param.max() != null && !param.max().isEmpty()) {
            double minVal = Double.parseDouble(param.min());
            double maxVal = Double.parseDouble(param.max());

            Slider slider = new Slider(minVal, maxVal, minVal);
            slider.setShowTickLabels(false);
            slider.setShowTickMarks(false);

            Label lblValoreLive = new Label();
            lblValoreLive.setStyle("-fx-font-weight: bold; -fx-text-fill: #2980b9; -fx-font-size: 14px;");
            lblValoreLive.setPrefWidth(45);

            if (valoreCorrente != null) {
                try {
                    double val = Double.parseDouble(valoreCorrente);
                    slider.setValue(val);
                    lblValoreLive.setText(String.valueOf((int) val));
                } catch (NumberFormatException ex) {
                    slider.setValue(minVal);
                    lblValoreLive.setText(String.valueOf((int) minVal));
                }
            } else {
                lblValoreLive.setText(String.valueOf((int) minVal));
            }

            slider.valueProperty().addListener((obs, oldVal, newVal) -> {
                lblValoreLive.setText(String.valueOf(newVal.intValue()));
            });

            slider.setOnMouseReleased(e -> {
                String nuovoValore = String.valueOf((int) slider.getValue());
                eseguiComando(controllerTargets, param.nome(), nuovoValore, idDispositivo);
            });

            HBox boxSliderEValore = new HBox(10, slider, lblValoreLive);
            boxSliderEValore.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(boxSliderEValore, Priority.ALWAYS);

            controlloGrafico = boxSliderEValore;

        } else {
            TextField testo = new TextField();
            if (valoreCorrente != null) testo.setText(valoreCorrente);

            testo.setOnAction(e -> eseguiComando(controllerTargets, param.nome(), testo.getText(), idDispositivo));
            controlloGrafico = testo;
        }

        controlloGrafico.setDisable(param.readOnly());

        HBox rigaParametro = new HBox(15);
        rigaParametro.setAlignment(Pos.CENTER_LEFT);
        rigaParametro.getChildren().addAll(labelNome, controlloGrafico);

        return rigaParametro;
    }

    private static void eseguiComando(ControllerTargets controllerTargets, String parametro, String valore, String idTarget) {
    	try {
            controllerTargets.eseguiComando(parametro, valore, idTarget, "user");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("[Errore]: " + e.getMessage());
        } catch (Exception e) {
	    	System.err.println("[Errore Critico]: " + e.getMessage());
	    	e.printStackTrace();
        }
    }
}