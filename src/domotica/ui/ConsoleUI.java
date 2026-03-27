package domotica.ui;

import domotica.app.ControllerDispositivo;
import domotica.domain.ParamStato;

import java.util.List;
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
            System.out.println("--- NUOVO COMANDO ---");
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
                System.out.println("\n⏳ Elaborazione in corso...");
                
                // Chiamiamo il nostro Direttore d'Orchestra!
                List<ParamStato> risultati = controller.eseguiComando(parametro, valore, idTarget);

                // Stampiamo i risultati restituiti dalla rete
                System.out.println("✅ ESITO COMANDO:");
                if (risultati.isEmpty()) {
                    System.out.println("Nessun dispositivo aggiornato. (Target non compatibile o offline)");
                } else {
                    for (ParamStato p : risultati) {
                        System.out.println(" -> Confermato: " + p.getNome() + " impostato a " + p.getValore());
                    }
                }
            } catch (Exception e) {
                // Se il Controller o il Dominio lanciano un'eccezione critica, la UI la cattura e la mostra all'utente
                System.err.println("❌ ERRORE DI SISTEMA: " + e.getMessage());
            }
            System.out.println("\n-----------------------------------------");
        }
        
        scanner.close();
    }
}