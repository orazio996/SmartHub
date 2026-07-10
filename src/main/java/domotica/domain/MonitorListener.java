package domotica.domain;

/**
 * Chi implementa questa interfaccia può ascoltare il ControllerMonitoraggio per transizioni di stato o altri eventi.
 */
public interface MonitorListener {
    
    /**
     * Si attiva ogni volta che un dispositivo cambia il suo stato.
     */
    void onEvento(Evento e);
    void onErrore(String errore);
}