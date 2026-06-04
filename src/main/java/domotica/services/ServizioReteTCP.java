package domotica.services;

//import domotica.domain.ParamStato;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
    public void send(RichiestaSH req, String dest) throws Exception {
		
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
                throw new Exception("Nessuna risposta ricevuta.");
            }

            JsonObject jsonRes = JsonParser.parseString(risposta).getAsJsonObject();
            
            if (jsonRes.has("status") && jsonRes.get("status").getAsString().equals("ERROR")) {
                String msgErrore = jsonRes.has("msg") ? jsonRes.get("msg").getAsString() : "Errore sconosciuto";
                throw new Exception("Comando rifiutato dal dispositivo: " + msgErrore);
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


	                } catch (Exception e) {
	                    System.err.println("[RETE] Pacchetto scartato per errore: " + e.getMessage());
	                }
	            }
	        } catch (Exception e) {
	            System.err.println("[RETE] Impossibile aprire il ServerSocket sulla porta " + portaMonitoraggio);
	            e.printStackTrace();
	        }
	    }).start();
	}
}
//	@Override
//	public CompletableFuture<String> listen() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
//	private ParamStato parseRisposta(String res) throws Exception {
//		
//
//        String response = res.replaceAll("\\s+", ""); 
//        if (response.contains("\"status\":\"OK\"")) {
//        	
//        	String parametro = null;
//        	String nuovoValore = null;
//        	
//        	if (response.contains("\"parametro\":\"")) {
//                int pStart = response.indexOf("\"parametro\":\"") + 13;
//                int pEnd = response.indexOf("\"", pStart);
//                if (pStart > 12 && pEnd > pStart) {
//                    parametro = response.substring(pStart, pEnd);
//                }
//            }
//            
//            if (response.contains("\"valore\":\"")) {
//                int startIndex = response.indexOf("\"valore\":\"") + 10;
//                int endIndex = response.indexOf("\"", startIndex);
//                if (startIndex > 9 && endIndex > startIndex) {
//                    nuovoValore = response.substring(startIndex, endIndex);
//                }
//            }
//            return new ParamStato(parametro, nuovoValore);        
//            
//        } else {
//            // es. risposta: {"status":"ERROR", "msg":"Valore troppo alto"})
//            throw new Exception("Il dispositivo ha rifiutato il comando: " + res);
//        }
//	}
//
//}
