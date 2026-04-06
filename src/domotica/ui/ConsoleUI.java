package domotica.ui;

import domotica.app.ControllerDispositivo;
import domotica.domain.TransizioneStato;

import java.util.Scanner;

/**
 * Interfaccia CLI per interagire con il sistema.
 */
public class ConsoleUI {

    private ControllerDispositivo controller;

    public ConsoleUI(ControllerDispositivo controller) {
        this.controller = controller;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================");
        System.out.println("   🏠 HUB DOMOTICO SYSTEM - AVVIATO");
        System.out.println("=========================================\n");

        while (true) {
            System.out.println("NUOVO COMANDO");
            System.out.print("Inserisci ID Target (es. 'Luce_Salotto' o 'exit' per uscire): ");
            String idTarget = scanner.nextLine();

            if (idTarget.equalsIgnoreCase("exit")) {
                System.out.println("Spegnimento dell'Hub Domotico in corso...");
                break;
            }

            System.out.print("Inserisci parametro da modificare (es. 'luminosita'): ");
            String parametro = scanner.nextLine();

            System.out.print("Inserisci il nuovo valore (es. '80'): ");
            String valore = scanner.nextLine();

            try {
                controller.eseguiComando(parametro, valore, idTarget)
                    .thenAccept(risultati -> {
                    	
                        if (risultati.isEmpty()) {
                            System.out.println("❌[Info]" + idTarget + ": Nessun dispositivo aggiornato");
                        } else {
                        	for(TransizioneStato t: risultati) {
                        		System.out.println(t.getIdDispositivo() + ": " + t.getParam() + " " + t.getNewVal());
                        	}
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("\n❌[ERRORE] Problema di rete: " + ex.getMessage());
                        return null; 
                    });

            } catch (Exception e) {
                System.err.println("❌ [ERRORE]: " + e.getMessage());
            }
            System.out.println("\n-----------------------------------------");
        }
        
        scanner.close();
    }
}