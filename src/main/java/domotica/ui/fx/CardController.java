package domotica.ui.fx;

import domotica.app.ControllerTargets;
import domotica.ui.dto.DescParametroDTO;
import domotica.ui.dto.DispositivoDTO;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.Map;

public class CardController {

    @FXML private Label lblNomeCard;
    @FXML private VBox contenitoreControlliCard;

    public void initDati(DispositivoDTO dispositivo, ControllerTargets controllerTargets) {
        lblNomeCard.setText("🔌 " + dispositivo.id());
        contenitoreControlliCard.getChildren().clear();

        if (dispositivo.parametri() != null) {
            for (Map.Entry<String, DescParametroDTO> entry : dispositivo.parametri().entrySet()) {
                Node rigaControllo = ControlloGraficoFactory.creaControllo(
                    dispositivo.id(), entry.getValue(), 
                    dispositivo.statoAttuale() != null ? dispositivo.statoAttuale().get(entry.getKey()) : null, 
                    controllerTargets
                );
                contenitoreControlliCard.getChildren().add(rigaControllo);
            }
        }
    }
}