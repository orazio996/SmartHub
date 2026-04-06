package domotica.devices;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Simulatore di un Termostato Smart.
 */
public class Termostato implements Runnable{
	
	private int porta;
    private String MAC;
    private String tipo;
    private String marca;
    private String modello;
	private static Map<String, String> stato;
	//private Map<String, descParam> descrizioneStato
	
	public Termostato(int porta, String MAC, String tipo, String marca, String modello) {
		this.porta = porta;
		this.MAC = MAC;
		this.tipo = tipo;
		this.marca = marca;
		this.modello = modello;
		stato = new HashMap<>();
		//init stato
        stato.put("power", "OFF");
        stato.put("temperatura", "25");
        stato.put("modalita", "AUTO");
	}

	@Override
    public void run() {      
        
        System.out.println("💡 Dispositivo " + MAC + " Avviato!");
        System.out.println("🏷️  Tipo: " + tipo + " | Marca: " + marca + " | Modello: " + modello);
        System.out.println("🔋 Stato iniziale: " + stato);
        System.out.println("📡 In ascolto sulla porta " + porta + "...\n");

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            
            while (true) { 
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String richiesta = in.readLine();
                    if (richiesta == null) continue; // Evita crash se riceve ping vuoti

                    System.out.println("Ricevuto comando: " + richiesta);
                    
                    String nuovoValore = "sconosciuto";
                    String parametro = "sconosciuto";

                    //param
                    if (richiesta.contains("\"param\":\"")) {
                        int pStart = richiesta.indexOf("\"param\":\"") + 9;
                        int pEnd = richiesta.indexOf("\"", pStart);
                        parametro = richiesta.substring(pStart, pEnd);
                    }
                    //val
                    if (richiesta.contains("\"val\":\"")) {
                        int vStart = richiesta.indexOf("\"val\":\"") + 7;
                        int vEnd = richiesta.indexOf("\"", vStart);
                        nuovoValore = richiesta.substring(vStart, vEnd);
                    }
                    if (!parametro.equals("sconosciuto")) {
                        stato.put(parametro, nuovoValore);
                    }

                    System.out.println("[HARDWARE] Nuovo stato: " + stato);

                    //risposta
                    String rispostaJson = String.format(
                        "{\"status\":\"OK\", \"parametro\":\"%s\", \"valore\":\"%s\"}", 
                        parametro, nuovoValore
                    );
                    
                    out.println(rispostaJson);
                    System.out.println("[HARDWARE] Inviata conferma: " + rispostaJson + "\n");
                    
                } catch (Exception e) {
                    System.err.println("Errore di rete temporaneo: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}