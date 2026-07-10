package domotica.services;

//import domotica.domain.ParamStato;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Servizio di comunicazione tramite Socket TCP.
 * Invia le richieste ai dispositivi fisici e attende la risposta.
 */
public class ServizioReteTCP implements ServizioRete{

	private int portaMonitoraggio;
	
	public ServizioReteTCP(int porta) {
        this.portaMonitoraggio = porta;
    }
	
    /**
     * Apre una Socket verso l'indirizzo e la porta specificati, invia il comando
     * e attende una conferma dal dispositivo.
     */
	@Override
    public void send(RichiestaSH req, String dest) throws IOException {
		
		Objects.requireNonNull(dest, "La destinazione non può essere nulla");
		
		String[] parti = dest.split(":");
        if (parti.length != 2) {
            throw new IllegalArgumentException("Formato non valido. Formato atteso: IP:Porta");
        }
        String indirizzo = parti[0];
        int porta = Integer.parseInt(parti[1]);

        try (Socket socket = new Socket()) {
            
            socket.connect(new InetSocketAddress(indirizzo, porta), 2000);
            socket.setSoTimeout(2000); 

            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));


            String payload = req.toString();
            out.println(payload);

            String risposta = in.readLine();

            if (risposta == null) {
                throw new IOException("[ErroreRete] Nessuna risposta ricevuta.");
            }

            JsonObject jsonRes ;
            try {
                jsonRes = JsonParser.parseString(risposta).getAsJsonObject();
            } catch (JsonSyntaxException | IllegalStateException e) {
                throw new IOException("[ErroreRete] Formato risposta non valido.", e);
            }

            
            if (jsonRes.has("status") && jsonRes.get("status").getAsString().equals("ERROR")) {
                String msgErrore = jsonRes.has("msg") ? jsonRes.get("msg").getAsString() : "Errore sconosciuto";
                throw new IOException("[ErroreRete] Comando rifiutato dal dispositivo: " + msgErrore);
            }
            
            System.out.println("\n[RETE] Ricevuta risposta: " + risposta);

        }
    }
	
	@Override
	public void listen(MsgListener listener) {
	    
	    new Thread(() -> {
	        try (ServerSocket serverSocket = new ServerSocket(portaMonitoraggio)) {
	            System.out.println("[RETE] ServerMonitoraggio avviato sulla porta " + portaMonitoraggio);

	            while (true) {
	                try (
	                    Socket clientSocket = serverSocket.accept(); 
	                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
	                ) {
	                    String payloadJSON = reader.readLine();

	                    if (payloadJSON != null && !payloadJSON.isEmpty()) {

	                        JsonObject json = JsonParser.parseString(payloadJSON).getAsJsonObject();
	                        
	                        String idTarget = json.has("idTarget") ? json.get("idTarget").getAsString() : "UNKNOWN";
	                        String tipo = json.has("tipo") ? json.get("tipo").getAsString() : "UNKNOWN";

	                        listener.msgRete(idTarget, tipo, payloadJSON);
	                    }


	                } catch (IOException e) {
	                    System.err.println("[ErroreRete] Errore connessione col client");
	                } catch ( JsonParseException | IllegalStateException e) {
		            	System.err.println("[ErroreRete] Ricevuto messaggio non valido. Pacchetto scartato." + e.getMessage());
		            }
	            }
	        } catch (IOException e) {
	            System.err.println("[ErroreRete] Impossibile avviare il server sulla porta " + portaMonitoraggio);
	            listener.msgErrore("Impossibile avviare il server sulla porta " + portaMonitoraggio);
	        }
	    }).start();
	}
}

