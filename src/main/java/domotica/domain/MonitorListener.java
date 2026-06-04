package domotica.domain;

/**
 * Chi implementa questa interfaccia può ascoltare il ControllerMonitoraggio per transizioni di stato o altri eventi.
 */
public interface MonitorListener {
    
    /**
     * Si attiva ogni volta che un dispositivo cambia il suo stato.
     * @param ts: L'oggetto che contiene chi è cambiato, cosa è cambiato, il nuovo ed  il vecchio valore.
     */
    void onEvento(Evento e);
    
}