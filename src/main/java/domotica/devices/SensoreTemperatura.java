package domotica.devices;

	import java.io.BufferedReader;
	import java.io.InputStreamReader;
	import java.io.PrintWriter;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.util.HashMap;
	import java.util.Map;

	/**
	 * Simulatore di un sensore di temperatura.
	 */

public class SensoreTemperatura implements Runnable{
		
	private int porta;
    private String MAC;
    private String tipo;
    private String marca;
    private String modello;
	private Map<String, String> stato;
	//private Map<String, descParam> descrizioneStato
	
	// dovrei farmeli inviare dall hub
    private String hubIp = "127.0.0.1";
    private int portMonitoraggio = 5000; 
	
	public SensoreTemperatura(int porta, String MAC, String tipo, String marca, String modello) {
		this.porta = porta;
		this.MAC = MAC;
		this.tipo = tipo;
		this.marca = marca;
		this.modello = modello;
		stato = new HashMap<>();
		//init stato
        stato.put("power", "OFF");
        stato.put("temperatura", "25");
	}
	
	
	public void simulaLetturaAmbiente(String param, String val) {

        stato.put(param, val);
        System.out.println("\n[RILEVAZIONE] " + MAC + " rileva: " + param + " = " + val);

        String msg = String.format(
            "{\"idTarget\":\"%s\", \"tipo\":\"CAMBIO_STATO\", \"parametro\":\"%s\", \"valore\":\"%s\"}", 
            MAC, param, val
        );
        toHub(msg);
    }
	
	// metodo invio messaggi
    private void toHub(String jsonMessaggio) {
        try (Socket socket = new Socket(hubIp, portMonitoraggio);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            out.println(jsonMessaggio);
            
        } catch (Exception e) {
            System.err.println("[" + MAC + "] Impossibile raggiungere l'Hub per inviare dati.");
        }
    }

    
    // metodo ping ogni 5 secondi
    private void pingStart() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); 
                    
                    String jsonPing = String.format("{\"idTarget\":\"%s\", \"tipo\":\"PING\"}", MAC);
                    toHub(jsonPing);
                    
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
	
	
	

	@Override
    public void run() {      
        
        System.out.println("💡 Dispositivo " + MAC + " Avviato!");
        System.out.println("🏷️  Tipo: " + tipo + " | Marca: " + marca + " | Modello: " + modello);
        System.out.println("🔋 Stato iniziale: " + stato);
        System.out.println("📡 In ascolto sulla porta " + porta + "...\n");
        
        pingStart();

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            
            while (true) { 
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String richiesta = in.readLine();
                    if (richiesta == null) continue; 

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
                        
                     // invio cambio stato
                        String jsonCambioStato = String.format(
                            "{\"idTarget\":\"%s\", \"tipo\":\"CAMBIO_STATO\", \"parametro\":\"%s\", \"valore\":\"%s\"}", 
                            MAC, parametro, nuovoValore
                        );
                        toHub(jsonCambioStato);
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
