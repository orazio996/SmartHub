package domotica;

import domotica.app.ControllerDispositivo;
import domotica.domain.*;
import domotica.services.ServizioReteTCP;
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
        Dispositivo lampadaScrivania = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        
        // mock setup lampadina1
        Map<String, DescParametro> descParamsLamp1 = new HashMap<>();
        descParamsLamp1.put("luminosita", new DescParametro("luminosita", "int", "%", false, "0", "100", List.of())); 
        descParamsLamp1.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        descParamsLamp1.put("colore", new DescParametro("colore", "string", "", false, "", "", List.of("Rosso", "Verde", "Blu"))); 
        descParamsLamp1.put("lampeggio", new DescParametro("lampeggio", "float", "sec", false, "0.2", "3s", List.of())); 
        DescDispositivo descLamp1 = new DescDispositivo("Tapoo", "Lampadina", "5E355 12W", descParamsLamp1);
        Dispositivo lampadaStudio = new Dispositivo("LampadaNotte", "127.0.0.1:8081", descLamp1);
        
        // mock setup termostato
        Map<String, DescParametro> descParamsTerm = new HashMap<>();
        descParamsTerm.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        descParamsTerm.put("temperatura", new DescParametro("temperatura", "float", "℃", false, "0", "30", List.of())); 
        descParamsTerm.put("modalita", new DescParametro("modalita", "string", "", false, "", "", List.of("AUTO", "CALDO", "FREDDO"))); 
        DescDispositivo descTerm = new DescDispositivo("Sampsung", "Termostato", "SuperWarm 2000X", descParamsTerm);
        Dispositivo termostato = new Dispositivo("Termostato", "127.0.0.1:8090", descTerm);
        
        Gruppo luci = new Gruppo("luci", false, false);
        luci.addTarget(lampadaScrivania);
        luci.addTarget(lampadaStudio);
        Gruppo studio = new Gruppo("studio", false, true);
        studio.addTarget(termostato);
        studio.addTarget(lampadaStudio);
        
        
        // setup sistema
        RegistroTargets registro = new RegistroTargets();
        registro.addTarget(lampadaScrivania);
        registro.addTarget(lampadaStudio);
        registro.addTarget(termostato);
        registro.addTarget(luci);
        registro.addTarget(studio);
        Cronologia cronologia = new Cronologia();
        ServizioReteTCP servizioRete = new ServizioReteTCP();

        ControllerDispositivo controller = new ControllerDispositivo(registro, cronologia, servizioRete);
        
        // avvio ui
        ConsoleUI ui = new ConsoleUI(controller);
        ui.start();
    }
}