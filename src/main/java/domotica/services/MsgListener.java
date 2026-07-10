package domotica.services;

/**
 * Interfaccia per il pattern Observer.
 * Permette di ricevere messaggi dalla rete senza usare CompletableFuture 
 * e senza esporre il ControllerMonitoraggio.
 */
public interface MsgListener {
    void msgRete(String idTarget, String tipo, String payload);
    void msgErrore(String errore);
}