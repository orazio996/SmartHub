package domotica.services;

import domotica.domain.ParamStato;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Servizio di comunicazione tramite Socket TCP.
 * Invia le richieste ai dispositivi fisici e attende la risposta.
 */
public class ServizioIPC {

    /**
     * Apre una Socket verso l'indirizzo e la porta specificati, invia il comando
     * e attende una conferma dal dispositivo.
     */
    public ParamStato send(RichiestaIPC req, String indirizzo, int porta) throws Exception {

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

            System.out.println("[RETE] Ricevuta risposta: " + risposta);

            String response = risposta.replaceAll("\\s+", ""); 
            if (response.contains("\"status\":\"OK\"")) {
            	
            	String nuovoValore = null;
                
                // Estraggo il valore manualmente
                if (response.contains("\"valore\":\"")) {
                    int startIndex = response.indexOf("\"valore\":\"") + 10;
                    int endIndex = response.indexOf("\"", startIndex);
                    if (startIndex > 9 && endIndex > startIndex) {
                        nuovoValore = response.substring(startIndex, endIndex);
                    }
                }
                return new ParamStato(req.getParam(), nuovoValore);        
                
            } else {
                // es. risposta: {"status":"ERROR", "msg":"Valore troppo alto"})
                throw new Exception("Il dispositivo ha rifiutato il comando: " + risposta);
            }
        }
    }
}
