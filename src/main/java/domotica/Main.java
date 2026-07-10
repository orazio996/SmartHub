package domotica;

import domotica.ui.ConsoleUI;
import domotica.ui.fx.MainController;
import domotica.util.Context;
import domotica.util.JsonStartup;
import domotica.util.ServizioStartup;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class Main extends Application {


    @Override
    public void start(Stage primaryStage) {
        System.out.println("Inizializzazione Sistema");

        String cartellaData = "/resources/data"; 
        int portaRete = 5000;

        ServizioStartup startup = new JsonStartup(cartellaData, portaRete);

        try {
	        Context contesto = startup.avviaSistema();
	        new Thread(() -> {
	        	ConsoleUI ui = new ConsoleUI(contesto);
	            ui.start();
            }).start();
	
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/domotica/ui/fx/MainLayout.fxml"));
	        Parent root = loader.load();

	        MainController uiController = loader.getController();
	        contesto.getControllerMonitor().addMonitorListener(uiController);
	        uiController.setContesto(contesto);

	        primaryStage.setTitle("SmartHUB Control Panel");
            primaryStage.setScene(new Scene(root, 1280, 720)); 
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("ERRORE DURANTE L'AVVIO DEL SISTEMA:");
            e.printStackTrace();
            Platform.exit(); 
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}