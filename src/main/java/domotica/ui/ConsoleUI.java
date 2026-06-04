package domotica.ui;

import domotica.app.ControllerRoutines;
import domotica.app.ControllerTargets;
import domotica.domain.*;

import java.util.Scanner;

/**
 * Interfaccia CLI per interagire con il sistema.
 */
public class ConsoleUI {

    private ControllerTargets controllerTargets;
    private ControllerRoutines controllerRoutines;
    private RegistroTargets registro;
    private MotoreRoutine motoreRoutine;

    public ConsoleUI(ControllerTargets ct, ControllerRoutines cr, RegistroTargets r, MotoreRoutine eng) {
        this.controllerTargets = ct;
        this.controllerRoutines = cr;
        this.registro = r;
        this.motoreRoutine = eng;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================");
        System.out.println("   🏠 HUB DOMOTICO SYSTEM - AVVIATO");
        System.out.println("=========================================\n");

        while (true) {
            System.out.println("\n--- MENU PRINCIPALE ---");
            System.out.println("1. 🕹️ Invia Comando Diretto (Manuale)");
            System.out.println("2. 🤖 Crea Nuova Routine (Automazione)");
            System.out.println("3. 📋 Visualizza Routine Attive");
            System.out.println("4. 🎛️ Visualizza Target Registrati"); // <-- Nuova opzione!
            System.out.println("5. ❌ Esci");
            System.out.print("Scegli un'opzione: ");
            
            String scelta = scanner.nextLine();

            switch (scelta) {
                case "1":
                    menuInviaComando(scanner);
                    break;
                case "2":
                    menuCreaRoutine(scanner);
                    break;
                case "3":
                    stampaRoutines();
                    break;
                case "4":
                    stampaTargets(); // <-- Chiamata al nuovo metodo
                    break;
                case "5":
                    System.out.println("Spegnimento dell'Hub Domotico in corso...");
                    scanner.close();
                    return;
                default:
                    System.out.println("⚠️ Opzione non valida. Riprova.");
            }
        }
    }

    // ... (menuInviaComando e menuCreaRoutine rimangono identici a prima) ...
    private void menuInviaComando(Scanner scanner) {
        System.out.println("\n--- INVIA COMANDO ---");
        System.out.print("ID Target (es. 'LampadaScrivania'): ");
        String idTarget = scanner.nextLine();

        System.out.print("Parametro (es. 'power'): ");
        String parametro = scanner.nextLine();

        System.out.print("Valore (es. 'ON'): ");
        String valore = scanner.nextLine();

        try {
            controllerTargets.eseguiComando(parametro, valore, idTarget);
            System.out.println("✅ Comando inviato con successo!");
        } catch (Exception e) {
            System.err.println("❌ [ERRORE COMANDO]: " + e.getMessage());
        }
    }

    private void menuCreaRoutine(Scanner scanner) {
        System.out.println("\n--- CREAZIONE NUOVA ROUTINE ---");
        try {
            System.out.print("Nome della Routine: ");
            String nome = scanner.nextLine();

            System.out.print("Tipo (TIME / EVENT): ");
            String tipo = scanner.nextLine().toUpperCase();

            System.out.print("ID Target da comandare (es. 'Termostato'): ");
            String targetAzione = scanner.nextLine();
            System.out.print("Parametro da modificare (es. 'power'): ");
            String paramAzione = scanner.nextLine();
            System.out.print("Valore da impostare (es. 'ON'): ");
            String valAzione = scanner.nextLine();
            
            String cmdJson = String.format("{\"param\": \"%s\", \"valore\": \"%s\"}", paramAzione, valAzione);
            String triggerJson = "";

            if (tipo.equals("TIME")) {
                System.out.print("Orario di attivazione (formato HH:mm, es. 18:30): ");
                String orario = scanner.nextLine();
                
                System.out.print("Giorni di ripetizione in inglese separati da virgola (es. MONDAY, FRIDAY)\noppure premi INVIO per un'esecuzione singola: ");
                String giorni = scanner.nextLine();
                
                // Costruiamo il JSON in modo dinamico
                if (giorni.trim().isEmpty()) {
                    // Esecuzione singola (chiave "orario" esatta!)
                    triggerJson = String.format("{\"orario\": \"%s\"}", orario);
                } else {
                    // Esecuzione ripetuta: creiamo l'array JSON per i giorni
                    String[] arrayGiorni = giorni.split(",");
                    StringBuilder giorniArrayJson = new StringBuilder("[");
                    for (int i = 0; i < arrayGiorni.length; i++) {
                        giorniArrayJson.append("\"").append(arrayGiorni[i].trim().toUpperCase()).append("\"");
                        if (i < arrayGiorni.length - 1) {
                            giorniArrayJson.append(", ");
                        }
                    }
                    giorniArrayJson.append("]");
                    
                    // Uniamo orario e array di giorni
                    triggerJson = String.format("{\"orario\": \"%s\", \"giorniRipetizione\": %s}", orario, giorniArrayJson.toString());
                }
                
            } else if (tipo.equals("EVENT")) {
                System.out.print("ID Target da osservare (es. 'Termostato'): ");
                String targetOsservato = scanner.nextLine();
                System.out.print("Parametro da osservare (es. 'temperatura'): ");
                String paramOsservato = scanner.nextLine();
                System.out.print("Operatore (<, >, =, !=): ");
                String operatore = scanner.nextLine();
                System.out.print("Soglia di scatto (es. 18): ");
                String soglia = scanner.nextLine();
                
                triggerJson = String.format(
                    "{\"targetOsservato\": \"%s\", \"paramOsservato\": \"%s\", \"operatore\": \"%s\", \"soglia\": \"%s\"}", 
                    targetOsservato, paramOsservato, operatore, soglia
                );
            } else {
                System.out.println("❌ Tipo routine non valido. Creazione annullata.");
                return;
            }

            controllerRoutines.addRoutine(nome, tipo, targetAzione, cmdJson, triggerJson);
            System.out.println("✅ Routine creata e attivata con successo!");

        } catch (Exception e) {
            System.err.println("❌ [ERRORE ROUTINE]: " + e.getMessage());
        }
    }

    private void stampaRoutines() {
        System.out.println("\n--- ROUTINE ATTIVE NEL MOTORE ---");
        if (motoreRoutine.getRoutines().isEmpty()) {
            System.out.println("Nessuna routine presente.");
            return;
        }
        
        for (Routine r : motoreRoutine.getRoutines()) {
            String stato = r.isAbilitata() ? "🟢 ATTIVA" : "🔴 DISABILITATA";
            System.out.println("- [" + stato + "] " + r.getNome() + " (Target: " + r.getTarget().getId() + ")");
        }
    }

    // =====================================================================
    // NUOVO METODO: STAMPA I TARGET
    // =====================================================================
    private void stampaTargets() {
        System.out.println("\n--- TARGET REGISTRATI NEL SISTEMA ---");
        
        // Sostituisci getTargets() con il nome reale del metodo nel tuo RegistroTargets!
        // Potrebbe essere getTargetList(), getDispositivi(), getAll()...
        if (registro.getAllTargets().isEmpty()) {
            System.out.println("Nessun dispositivo o gruppo registrato.");
            return;
        }
        
        for (Target t : registro.getAllTargets()) {
            // Se hai un metodo getTipo() o se vuoi distinguere tra Gruppo e Dispositivo:
            String tipo = t.getClass().getSimpleName(); 
            System.out.println("- 🔌 ID: " + t.getId() + " [" + tipo + "]");
        }
    }
}