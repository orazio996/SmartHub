package domotica.devices;

import java.util.Scanner;

public class SimulatoreDispositivi {
    public static void main(String[] args) {
        System.out.println("INIZIALIZZAZIONE DISPOSITIVI...\n");

        Lampadina lampadaScrivania = new Lampadina(8080, "LampadaScrivania", "Lampadina", "Philips", "Hue White 9W");
        Lampadina lamapdaStudio  = new Lampadina(8081, "LampadaStudio", "Lampadina", "Tapoo", "5E355 12W");
        Termostato termostato = new Termostato(8090, "Termostato", "Termostato", "Samsung", "SuperWarm 2000X");
        SensoreTemperatura sensoreTemp = new SensoreTemperatura(8091, "SensoreTemp", "Sensore", "Samssung", "st234");

        new Thread(lampadaScrivania).start();
        new Thread(lamapdaStudio).start();
        new Thread(termostato).start();
        new Thread(sensoreTemp).start();
        
        Scanner scanner = new Scanner(System.in);
        System.out.println("===============================");
        System.out.println("   SIMULAZIONE DISPOSITIVI");
        System.out.println("===============================\n");

        while (true) {
            System.out.println("1. Cambia valore");
            System.out.println("2. Esci");
            System.out.print("Scegli un'opzione: ");

            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                	// per ora modifica un dispositivo specifico
                	System.out.print("Inserisci il parametro: ");
                    String parametro = scanner.nextLine();
                	
                	System.out.print("Inserisci il nuovo valore: ");
                    String nuovoValore = scanner.nextLine();

                    sensoreTemp.simulaLetturaAmbiente(parametro, nuovoValore);
                    System.out.println(parametro + " = " + nuovoValore + " msg inviato.");
                    break;

                case "2":
                    System.out.println("Spegnimento simulatore...");
                    System.exit(0);
                    break;

                default:
                    System.out.println("Opzione non valida.");
            }
        }
        
        
    }
}