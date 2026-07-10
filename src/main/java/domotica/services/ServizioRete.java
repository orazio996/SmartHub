package domotica.services;

import java.io.IOException;

/**
 * Interfaccia per i servizi di comunicazione verso i dispositivi.
 * Agnostico rispetto alla tecnologia sottostante.
 */
public interface ServizioRete {
    
    /**
     * Invia una richiesta: fireNforget.
     * @param req Il payload generato dallo Smart Hub
     * @param dest Il destinatario (es. "IP:Porta", "URL", "Topic MQTT")
     */
    void send(RichiestaSH req, String dest) throws IOException;
    void listen(MsgListener listener);     
//    CompletableFuture<String> request(RichiestaSH req, String dest);
//    CompletableFuture<String> awaitMsg();
    
    
}