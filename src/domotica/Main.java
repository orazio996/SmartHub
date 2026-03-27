package domotica;

import domotica.app.ControllerDispositivo;
import domotica.domain.*;
import domotica.services.ServizioIPC;
import domotica.ui.ConsoleUI;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Punto di ingresso dell'applicazione.
 * Inizializza il Dominio, i Servizi, il Controller e lancia la UI.
 */
public class Main {

    public static void main(String[] args) {
        
        // INIZIALIZZAZIONE
        
        // mock setup lampadina
        Map<String, DescParametro> descParamsLamp = new HashMap<>();
        descParamsLamp.put("luminosita", new DescParametro("luminosita", "int", "%", false, "0", "100", List.of())); 
        descParamsLamp.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        descParamsLamp.put("colore", new DescParametro("colore", "string", "", false, "", "", List.of("Rosso", "Verde", "Blu"))); 
        descParamsLamp.put("lampeggio", new DescParametro("lampeggio", "float", "sec", false, "0.2", "3s", List.of())); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue White 9W", descParamsLamp);
        Dispositivo lampadaScrivania = new Dispositivo("LampadaScrivania", "127.0.0.1", 8080, descLamp);
        
        
        // setup sistema
        RegistroTargets registro = new RegistroTargets();
        registro.addTarget(lampadaScrivania);
        Cronologia cronologia = new Cronologia();
        ServizioIPC servizioRete = new ServizioIPC();

        ControllerDispositivo controller = new ControllerDispositivo(registro, cronologia, servizioRete);
        
        // avvio ui
        ConsoleUI ui = new ConsoleUI(controller);
        ui.start();
    }
}