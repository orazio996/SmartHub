package domotica.devices;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SimulatoreDispositivi {
    public static void main(String[] args) {
        System.out.println("INIZIALIZZAZIONE DISPOSITIVI...\n");

        Lampadina lampadaScrivania = new Lampadina(8080, "LampadaScrivania", "Lampadina", "Philips", "Hue White 9W");
        Lampadina lampadaSalotto = new Lampadina(8081, "LampadaSalotto", "Lampadina RGB", "Philips Hue", "Hue Color 15W");
        Termostato termostatoSalotto = new Termostato(8090, "TermostatoSalotto", "Termostato", "Samsung", "SuperWarm 2000X");
        SensoreTemperatura sensoreTemp = new SensoreTemperatura(8091, "SensoreClima", "Sensore", "Samssung", "st234");
        Serranda serranda = new Serranda(8092, "Serranda", "SerrandaSmart", "SuperHome", "3TMega");
        
        // Registrazione polimorfica nella mappa
        Map<String, DispositivoSimulabile> mappaDispositivi = new HashMap<>();
        mappaDispositivi.put("LampadaScrivania", lampadaScrivania);
        mappaDispositivi.put("LampadaSalotto", lampadaSalotto);
        mappaDispositivi.put("TermostatoSalotto", termostatoSalotto);
        mappaDispositivi.put("SensoreClima", sensoreTemp);
        mappaDispositivi.put("Serranda", serranda);

        new Thread(lampadaScrivania).start();
        new Thread(lampadaSalotto).start();
        new Thread(termostatoSalotto).start();
        new Thread(sensoreTemp).start();
        new Thread(serranda).start();
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("===============================");
        System.out.println("   SIMULATORE ATTIVO");
        System.out.println("   Formato: [idTarget] [param] [valore]");
        System.out.println("   Esempio: LampadaScrivania power OFF");
        System.out.println("   Digita 'exit' per uscire");
        System.out.println("===============================\n");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("exit")) {
                System.out.println("Spegnimento simulatore...");
                scanner.close();
                System.exit(0);
            }

            String[] comando = input.split(" ");
            
            if (comando.length != 3) {
                System.out.println("Errore di sintassi. Usa il formato: idTarget parametro valore");
                continue;
            }

            String target = comando[0];
            String param = comando[1];
            String val = comando[2];

            DispositivoSimulabile dispositivo = mappaDispositivi.get(target);

            if (dispositivo != null) {
                dispositivo.simulaCambiamentoFisico(param, val);
            } else {
                System.out.println("Errore: Dispositivo '" + target + "' non trovato.");
            }
        }
    }
}