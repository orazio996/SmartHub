package domotica.ui;

import domotica.app.ControllerMetriche;
import domotica.app.ControllerRoutines;
import domotica.app.ControllerSequenze;
import domotica.app.ControllerTargets;
import domotica.domain.*;
import domotica.util.Context;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

/**
 * Interfaccia CLI per interagire con il sistema.
 */
public class ConsoleUI {

    private ControllerTargets controllerTargets;
    private ControllerRoutines controllerRoutines;
    private ControllerMetriche controllerMetriche;
    private ControllerSequenze controllerSeq;
    private RegistroTargets registro;
    private MotoreRoutine motoreRoutine;
    private Cronologia cronologia;

    public ConsoleUI(Context contesto) {
    	this.controllerTargets = contesto.getControllerTargets();
    	this.controllerRoutines = contesto.getControllerRoutines();
    	this.controllerMetriche = contesto.getControllerMetriche();
    	this.controllerSeq = contesto.getControllerSequenze();
    	this.registro = contesto.getRegistro();
    	this.motoreRoutine = contesto.getMotoreRoutine();
    	this.cronologia = contesto.getCronologia();
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("=========================================");
        System.out.println("     HUB DOMOTICO SYSTEM - AVVIATO");
        System.out.println("=========================================\n");

        while (true) {
            System.out.println("\n--- MENU PRINCIPALE ---");
            System.out.println("1. Invia Comando ");
            System.out.println("2. Crea Nuova Routine");
            System.out.println("3. Visualizza Routine Attive");
            System.out.println("4. Visualizza Target Registrati");
            System.out.println("5. Genera Report"); 
            System.out.println("6. esegui Sequenza"); 
            System.out.println("7. Annulla ultimo comando"); 
            System.out.println("8. Mostra cronologia"); 
            System.out.println("9. Esci");
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
                	menuGeneraReport(scanner);
                    break;
                case "6":
                    eseguiSequenza(scanner);
                	break;
                case "7":
					annullaUltimoComando();
                	break;
                case "8":
                	mostraCronologia();
                	break;
                case "9":
                    System.out.println("Spegnimento dell'Hub Domotico in corso...");
                    scanner.close();
                    return;
                default:
                    System.out.println("⚠️ Opzione non valida. Riprova.");
            }
        }
    }

    private void menuInviaComando(Scanner scanner) {
        System.out.println("\n--- INVIA COMANDO ---");
        System.out.print("ID Target (es. 'LampadaScrivania'): ");
        String idTarget = scanner.nextLine();

        System.out.print("Parametro (es. 'power'): ");
        String parametro = scanner.nextLine();

        System.out.print("Valore (es. 'ON'): ");
        String valore = scanner.nextLine();

        try {
            controllerTargets.eseguiComando(parametro, valore, idTarget, "user");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("[Errore]: " + e.getMessage());
        } catch (Exception e) {
	    	System.err.println("[Errore Critico]: " + e.getMessage());
	    	e.printStackTrace();
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
                
                if (giorni.trim().isEmpty()) {

                    triggerJson = String.format("{\"orario\": \"%s\"}", orario);
                } else {

                    String[] arrayGiorni = giorni.split(",");
                    StringBuilder giorniArrayJson = new StringBuilder("[");
                    for (int i = 0; i < arrayGiorni.length; i++) {
                        giorniArrayJson.append("\"").append(arrayGiorni[i].trim().toUpperCase()).append("\"");
                        if (i < arrayGiorni.length - 1) {
                            giorniArrayJson.append(", ");
                        }
                    }
                    giorniArrayJson.append("]");
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
                System.out.println("Tipo routine non valido. Creazione annullata.");
                return;
            }

            controllerRoutines.addRoutine(nome, tipo, targetAzione, cmdJson, triggerJson);
            System.out.println("Routine creata e attivata con successo!");

        } catch (Exception e) {
            System.err.println("[ERRORE ROUTINE]: " + e.getMessage());
        }
    }

    private void stampaRoutines() {
        System.out.println("\n--- ROUTINE ATTIVE NEL MOTORE ---");
        if (motoreRoutine.getRoutines().isEmpty()) {
            System.out.println("Nessuna routine presente.");
            return;
        }
        
        for (Routine r : motoreRoutine.getRoutines()) {
            String stato = r.isAbilitata() ? "ATTIVA" : "DISABILITATA";
            System.out.println("- [" + stato + "] " + r.getNome() + " (Target: " + r.getTarget().getId() + ")");
        }
    }

    private void stampaTargets() {
        System.out.println("\n--- TARGET REGISTRATI NEL SISTEMA ---");

        if (registro.getAllTargets().isEmpty()) {
            System.out.println("Nessun dispositivo o gruppo registrato.");
            return;
        }
        for (Target t : registro.getAllTargets()) {
            String tipo = t.getClass().getSimpleName(); 
            System.out.println("- ID: " + t.getId() + " [" + tipo + "]");
        }
    }
    
    private void eseguiSequenza(Scanner scanner) {
        System.out.println("\n=== ESECUZIONE MACRO/SEQUENZA ===");

        List<String> sequenzeDisponibili = controllerSeq.getSequenze();

        if (sequenzeDisponibili.isEmpty()) {
            System.out.println("Nessuna sequenza registrata nel sistema.");
            return;
        }

        System.out.println("Sequenze disponibili:");
        for (String idSeq : sequenzeDisponibili) {
            System.out.println(" 🔸 " + idSeq);
        }
        System.out.print("\nInserisci l'ID della sequenza da eseguire: ");
        String idSequenzaScelta = scanner.nextLine().trim();
        
        System.out.print("Inserisci l'ID del Target (Dispositivo o Gruppo): ");
        String idTargetScelto = scanner.nextLine().trim();

        System.out.println("\nAvvio esecuzione in corso...");
        
        new Thread(() -> {
        	try {
                controllerSeq.eseguiSequenza(idSequenzaScelta, idTargetScelto);
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.err.println("Errore: " + e.getMessage());  
            } catch (Exception e) {
                System.err.println("Errore critico: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
        
    }
    
    
    private void menuGeneraReport(Scanner scanner) {
        System.out.println("\n--- CONSULTA METRICHE (GENERA REPORT) ---");
        try {
            System.out.print("ID Target (es. 'LampadaScrivania'): ");
            String idTarget = scanner.nextLine();
            
            System.out.print("Parametro (es. 'Temperatura'): ");
            String param = scanner.nextLine();

            System.out.print("Tipo Metrica (es. 'Consumo'): ");
            String tipoMetrica = scanner.nextLine();

            System.out.print("Data Inizio (formato YYYY-MM-DD, es. 2026-06-01): ");
            LocalDate dataInizio = LocalDate.parse(scanner.nextLine());

            System.out.print("Data Fine (formato YYYY-MM-DD, es. 2026-06-16): ");
            LocalDate dataFine = LocalDate.parse(scanner.nextLine());

            System.out.print("Nome da assegnare al Report: ");
            String nomeReport = scanner.nextLine();

            Report reportGenerato = controllerMetriche.generaReport(idTarget, param, List.of(tipoMetrica), dataInizio, dataFine);
            controllerMetriche.salvaReport(reportGenerato, nomeReport);
            System.out.println("\n Report Generato con Successo!");
            System.out.println("Nome Report: " + reportGenerato.getNome());
            System.out.println("--- Risultati ---");
            
            if (reportGenerato.getMetriche().isEmpty()) {
                System.out.println("Nessun dato trovato per il periodo selezionato.");
            } else {
                for (Metrica m : reportGenerato.getMetriche()) {
                    System.out.println("- " + m.getTipoMetrica() + ": " + m.getValore() + " " + m.getUnitaMisura());
                }
            }

        } catch (Exception e) {
            System.err.println(" [ERRORE GENERAZIONE REPORT]: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void annullaUltimoComando() {
    	try {
    		controllerTargets.annullaUltimoComando();
    	} catch (IllegalArgumentException | IllegalStateException e) {
            System.err.println("Errore: " + e.getMessage());  
        } catch (Exception e) {
            System.err.println("Errore critico: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void mostraCronologia() {
    	for(Evento e : this.cronologia.getCronologia()) {
    		System.out.print(e.getSourceTimestamp() + " ");
    		System.out.print(e.getTimestamp() + " ");
    		System.out.print(e.getTipo() + " ");
    		for(TransizioneStato t : e.getTransizioni()) {
    			System.out.print(t.getIdDispositivo() + " ");
        		System.out.print(t.getParam() + " ");
        		System.out.println(t.getNewVal() + " ");
        		System.out.println("");
    		}
    	}
    }
}