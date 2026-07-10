package domotica.ui.fx;

import java.util.List;
import domotica.app.ControllerTargets;
import domotica.domain.Evento;
import domotica.domain.MonitorListener;
import domotica.ui.dto.DispositivoDTO;
import domotica.ui.dto.GruppoDTO;
import domotica.ui.dto.TargetDTO;
import domotica.util.Context;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import domotica.domain.TransizioneStato;
import javafx.scene.layout.StackPane;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.ListView;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;

public class MainController implements MonitorListener {

	@FXML private TreeView<TargetDTO> alberoTargets; 
    @FXML private StackPane contenitoreCentrale;
    @FXML private ListView<String> listaCronologia;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
    
    private ControllerTargets controllerTargets;
    private Context contesto;

    @FXML
    public void initialize() {
        alberoTargets.setShowRoot(false); 

        alberoTargets.setCellFactory(param -> new TreeCell<TargetDTO>() {
            @Override
            protected void updateItem(TargetDTO item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    String icona = item.tipo().equals("GRUPPO") ? "📁 " : "🔌 ";
                    setText(icona + item.id());
                }
            }
        });

        alberoTargets.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                TreeItem<TargetDTO> itemSelezionato = alberoTargets.getSelectionModel().getSelectedItem();
                if (itemSelezionato != null && itemSelezionato.getValue() != null) {
                    caricaPannelloCentrale(itemSelezionato.getValue());
                }
            }
        });
    }

    public void setContesto(Context contesto) {
        this.contesto = contesto;
        this.controllerTargets = contesto.getControllerTargets();
        
        popolaLista();
    }

    private void popolaLista() {
        if (controllerTargets != null) {
            List<TargetDTO> dati = controllerTargets.getTargetsPerUI();
            
            // ordinamento
            dati.sort((t1, t2) -> {
                if (t1.tipo().equals(t2.tipo())) {
                    return t1.id().compareToIgnoreCase(t2.id()); // Ordine alfabetico se sono uguali
                }
                return t1.tipo().equals("GRUPPO") ? -1 : 1; 
            });

            TreeItem<TargetDTO> root = new TreeItem<>(null);

            for (TargetDTO dto : dati) {
                root.getChildren().add(creaNodoAlbero(dto));
            }
            
            alberoTargets.setRoot(root);
        }
    }
    
    private TreeItem<TargetDTO> creaNodoAlbero(TargetDTO target) {
        TreeItem<TargetDTO> nodo = new TreeItem<>(target);

        if (target instanceof GruppoDTO) {
            GruppoDTO gruppo = (GruppoDTO) target;
            if (gruppo.figli() != null) {
                for (TargetDTO figlio : gruppo.figli()) {
                    nodo.getChildren().add(creaNodoAlbero(figlio));
                }
            }
        }
        return nodo;
    }
    
    @FXML
    private void annullaComando() {
    	try {
    		controllerTargets.annullaUltimoComando();
    	} catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Errore: " + e.getMessage());  
        } catch (Exception e) {
            System.err.println("Errore critico: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void caricaPannelloCentrale(TargetDTO target) {
        contenitoreCentrale.getChildren().clear();
        try {
            if (target.tipo().equals("DISPOSITIVO")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/domotica/ui/fx/DettaglioDispositivo.fxml"));
                Parent view = loader.load();
                DispositivoController controller = loader.getController();
                controller.initDati((DispositivoDTO) target, controllerTargets, contesto.getControllerSequenze(), contesto.getControllerMetriche());
                contenitoreCentrale.getChildren().add(view);
                
            } else if (target.tipo().equals("GRUPPO")) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/domotica/ui/fx/DettaglioGruppo.fxml"));
                Parent view = loader.load();
                GruppoController controller = loader.getController();
                controller.initDati((GruppoDTO) target, this, controllerTargets, contesto.getControllerSequenze(), contesto.getControllerMetriche());
                contenitoreCentrale.getChildren().add(view);
            }
        } catch (Exception e) {
            System.err.println("[Errore caricamento vista centrale]: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void selezionaTargetDaCard(TargetDTO target) {
        TreeItem<TargetDTO> nodoDaSelezionare = cercaNodo(alberoTargets.getRoot(), target.id());
        
        if (nodoDaSelezionare != null) {
            alberoTargets.getSelectionModel().select(nodoDaSelezionare);
            alberoTargets.scrollTo(alberoTargets.getSelectionModel().getSelectedIndex());
            
            caricaPannelloCentrale(nodoDaSelezionare.getValue()); 
        }
    }

    private TreeItem<TargetDTO> cercaNodo(TreeItem<TargetDTO> root, String idTarget) {
        if (root == null) return null;
        if (root.getValue() != null && root.getValue().id().equals(idTarget)) {
            return root;
        }
        for (TreeItem<TargetDTO> figlio : root.getChildren()) {
            TreeItem<TargetDTO> trovato = cercaNodo(figlio, idTarget);
            if (trovato != null) return trovato;
        }
        return null;
    }
    
    @FXML
    private void apriFormRoutine() {
        contenitoreCentrale.getChildren().clear();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/domotica/ui/fx/CreaRoutine.fxml"));
            Parent view = loader.load();

            CreaRoutineController formController = loader.getController();
            formController.setControllerRoutines(contesto.getControllerRoutines());

            contenitoreCentrale.getChildren().add(view);
        } catch (Exception e) {
            System.err.println("[Errore caricamento form routine]: " + e.getMessage());
        }
    }
    
    @FXML
    private void apriArchivioReports() {
        if (contesto == null || contesto.getControllerMetriche() == null) {
            System.err.println("Controller Metriche non disponibile.");
            return;
        }
        
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/resources/domotica/ui/fx/ArchivioReports.fxml"));
            javafx.scene.Parent root = loader.load();
            
            ArchivioReportsController controller = loader.getController();
            controller.initDati(contesto.getControllerMetriche());

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.setTitle("Archivio Reports Salvati");
            stage.setScene(new javafx.scene.Scene(root, 700, 500));
            stage.show(); // Usiamo show() e non showAndWait() così l'utente può tenere l'archivio aperto mentre usa il resto dell'app
            
        } catch (Exception e) {
            System.err.println("Errore apertura archivio reports: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onEvento(Evento e) {
        Platform.runLater(() -> {
            TreeItem<TargetDTO> nodoSelezionato = alberoTargets.getSelectionModel().getSelectedItem();
            TargetDTO targetSelezionato = (nodoSelezionato != null) ? nodoSelezionato.getValue() : null;
            popolaLista();
            if (targetSelezionato != null) {
                selezionaTargetDaCard(targetSelezionato); 
            }

            if (e.getTransizioni() != null && !e.getTransizioni().isEmpty()) {
                String orario = timeFormatter.format(Instant.ofEpochMilli(e.getTimestamp()));
                
                for (TransizioneStato ts : e.getTransizioni()) {
                	String idTarget;
                	if(e.getIdTarget() != null) {
                		idTarget = e.getIdTarget();
                	} else {
                		idTarget = ts.getIdDispositivo();
                	}
                    String logLine = String.format("[%s][%s][%s] %s:%s | %s: %s -> %s", 
                    	Long.toHexString(e.getSourceTimestamp()).toUpperCase(),
                    	e.getTipo(),
	                    orario,
	                    idTarget,
	                    ts.getIdDispositivo(), 
	                    ts.getParam(), 
	                    ts.getOldVal(), 
	                    ts.getNewVal()
                    );
                    listaCronologia.getItems().add(0, logLine); // Inserisce in cima alla lista
                }
            }
        });
    }

    @Override
    public void onErrore(String errore) {
        Platform.runLater(() -> System.err.println("Errore sistema: " + errore));
    }
}