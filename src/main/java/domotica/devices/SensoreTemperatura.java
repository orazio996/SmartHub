package domotica.devices;

	import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
	import java.io.PrintWriter;
	import java.net.ServerSocket;
	import java.net.Socket;
	import java.util.HashMap;
	import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

	/**
	 * Simulatore di un sensore di temperatura.
	 */

public class SensoreTemperatura implements Runnable, DispositivoSimulabile{
		
	private int porta;
    private String MAC;
    private String tipo;
    private String marca;
    private String modello;
	private Map<String, String> stato;
	private boolean statoConnessione = false;
	private String myIp = "127.0.0.1";
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
        stato.put("temperatura", "25");
        stato.put("umidita", "53");
	}
	
	
	public void simulaCambiamentoFisico(String param, String valore) {
        this.stato.put(param, valore);
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        String json = String.format(
            "{\"sourceTarget\":\"%s\", \"idTarget\":\"%s\", \"tipo\":\"physicalCmd\", \"parametro\":\"%s\", \"valore\":\"%s\", \"source\":\"system\", \"sourceTimestamp\":\"%s\"}",
            this.MAC, this.MAC, param, valore, timestamp
        );
        
        try {
            toHub(json);
            System.out.println("\n[" + MAC + "] Modifica fisica simulata con successo: " + param + " = " + valore);
        } catch (IOException e) {
            System.err.println("\n[" + MAC + "] Errore: Impossibile notificare l'Hub del cambiamento fisico.");
        }
    }
	
	// metodo invio messaggi
    private void toHub(String jsonMessaggio) throws IOException {
        try (Socket socket = new Socket(hubIp, portMonitoraggio);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            
            out.println(jsonMessaggio);
            
        } catch (Exception e) {
            System.err.println("[" + MAC + "] Impossibile raggiungere l'Hub per inviare dati.");
            throw new IOException();
        }
    }

    
    // metodo ping ogni 5 secondi
    private void pingStart() {
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000); 
                    String jsonPing;
                    if(statoConnessione) {
		                jsonPing = String.format("{\"idTarget\":\"%s\", \"tipo\":\"PING\"}", MAC);
                    }else {
                    	jsonPing = String.format("{\"idTarget\":\"%s\", \"tipo\":\"PING_SYNC\", \"stato\": %s, \"indirizzo\":\"%s:%s\"}",
                    			MAC,
                    			new Gson().toJson(stato),
                    			myIp,
                    			porta);
                    }
                    toHub(jsonPing);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                	System.out.println("[" + MAC + "] Offline.");
                	statoConnessione = false;
                }
            }
        }).start();
    }
	
	
	

	@Override
    public void run() {      
        
        System.out.println("Dispositivo " + MAC + " Avviato!");
        System.out.println("Tipo: " + tipo + " | Marca: " + marca + " | Modello: " + modello);
        System.out.println("Stato iniziale: " + stato);
        System.out.println("In ascolto sulla porta " + porta + "...\n");
        
        pingStart();

        try (ServerSocket serverSocket = new ServerSocket(porta)) {
            
            while (true) { 
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    String richiesta = in.readLine();
                    if (richiesta == null) continue; 

                    System.out.println("Ricevuto comando: " + richiesta);

                    JsonObject json = JsonParser.parseString(richiesta).getAsJsonObject();
                     
                    String sourceTarget = json.has("sourceTarget") ? json.get("sourceTarget").getAsString() : "sconosciuto";
                    String source = json.has("source") ? json.get("source").getAsString() : "sconosciuto";
                    String reqType = json.has("tipo") ? json.get("tipo").getAsString() : "sconosciuto";
                    String sourceTimestamp = json.has("sourceTimestamp") ? json.get("sourceTimestamp").getAsString() : "0";
                    String parametro = json.has("param") ? json.get("param").getAsString() : "sconosciuto";
                    String nuovoValore = json.has("val") ? json.get("val").getAsString() : "sconosciuto";
                    
                    if(reqType.equals("PING_ACK")) {
                    	this.statoConnessione = true;
                    	out.println("{\"status\":\"OK\"}");
                    	continue;
                    }
                    if (reqType.contains("Cmd")) {
                        stato.put(parametro, nuovoValore);
                        
                     // invio cambio stato
                        String jsonCambioStato = String.format(
                            "{\"sourceTarget\":\"%s\", \"idTarget\":\"%s\", \"tipo\":\"%s\", \"parametro\":\"%s\", \"valore\":\"%s\","
                            + "\"source\":\"%s\", \"sourceTimestamp\":\"%s\"}", 
                            sourceTarget, MAC, reqType, parametro, nuovoValore, source, sourceTimestamp
                        );
                        toHub(jsonCambioStato);
                        
                        System.out.println("[" + MAC + "] Nuovo stato: " + stato);

                        //risposta
                        String rispostaJson = String.format(
                            "{\"status\":\"OK\", \"parametro\":\"%s\", \"valore\":\"%s\"}", 
                            parametro, nuovoValore
                        );
                        
                        out.println(rispostaJson);
                        System.out.println("[" + MAC + "] Inviata conferma: " + rispostaJson + "\n");
                        
                    } else {
                    	String erroreJson = String.format(
                            "{\"status\":\"ERROR\", \"msg\":\"Tipo richiesta sconosciuto: %s\"}", 
                            reqType
                        );
                        out.println(erroreJson);
                        System.err.println("[" + MAC + "] Richiesta rifiutata: " + reqType + "\n");
                    }
                    
                } catch (Exception e) {
                    System.err.println("Errore di rete temporaneo: " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
