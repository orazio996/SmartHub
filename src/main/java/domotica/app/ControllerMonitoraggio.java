package domotica.app;

import domotica.domain.*;
import domotica.services.*;

import java.util.Objects;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


public class ControllerMonitoraggio implements MsgListener{

    private RegistroTargets registro;
    private ServizioRete servizioRete;
    private ScheduledExecutorService timer;
    private List<MonitorListener> listeners;
    
    private static final long SOGLIA_OFFLINE = 10000;

    public ControllerMonitoraggio(RegistroTargets registro, ServizioRete servizioRete) {
        this.registro = Objects.requireNonNull(registro);
        this.servizioRete = Objects.requireNonNull(servizioRete);
        this.listeners = new ArrayList<>();
    }
    
    
    /**
     * Aggiunge un listener che riceve gli eventi captati.
     */
    public void addMonitorListener(MonitorListener l) {
    	if (l == null) {
            throw new IllegalArgumentException("Impossibile aggiungere un MonitorListener nullo.");
        }

        if (!listeners.contains(l)) {
            listeners.add(l);
        }
    }

    /**
     * Inizializzazione: Avvia i thread.
     */
    public void start() {
        System.out.println("[SISTEMA] Avvio Controller Monitoraggio...");

        servizioRete.listen(this); 

        this.timer = Executors.newSingleThreadScheduledExecutor();
        // verificaInattivita() ogni 5 secondi
        this.timer.scheduleAtFixedRate(() -> {
            verificaInattivita();
        }, 0, 5, TimeUnit.SECONDS);
        
        System.out.println("[SISTEMA] Watchdog e Server Rete avviati con successo.");
    }

    /**
     * IL WATCHDOG: Controlla chi è offline
     */
    private void verificaInattivita() {
        List<Dispositivo> dispositivi = registro.getDispositivi(); 
        long oraAttuale = System.currentTimeMillis();

        for (Dispositivo d : dispositivi) {
            long last = d.getLastSeen();

            if ((oraAttuale - last) > SOGLIA_OFFLINE && d.isOnline()) {
                
                System.out.println("[WATCHDOG] Dispositivo " + d.getId() + " è OFFLINE.");
                d.setStatoConn(false);

                Evento e = new Evento("DISCONNESSIONE", d.getId(), List.of());
                for (MonitorListener l : listeners) {
                	l.onEvento(e);
                }
                
            }
        }
    }

    /**
     * Riceve i messaggi dalla rete
     * Chiamato dal ServizioRete quando arriva un pacchetto
     */
    public void msgRete(String idTarget, String tipo, String payload) {
        
        Target t = registro.getTarget(idTarget);
        if (t == null || !(t instanceof Dispositivo)) {
            System.out.println("[RETE] Target sconosciuto o non valido: " + idTarget);
            return;
        }

        Dispositivo d = (Dispositivo) t;
        long oraAttuale = System.currentTimeMillis();

        if (tipo.equals("PING")) {
//            System.out.println("[RETE] Ping ricevuto da " + idTarget);
            d.setLastSeen(oraAttuale);
            
            // Se era offline, lo rimettiamo online
            if (!d.isOnline()) {
            	
                d.setStatoConn(true);
                System.out.println("[PING] Dispositivo " + d.getId() + " è ONLINE.");
                Evento e = new Evento("CONNESSIONE", d.getId(), List.of());
                for (MonitorListener l : listeners) {
                	l.onEvento(e);
                }
                
            }

        } else if (tipo.equals("CAMBIO_STATO")) {
            System.out.println("[RETE] Cambio stato per " + idTarget + ": " + payload);
            d.setLastSeen(oraAttuale); // Un cambio stato vale anche come segno di vita
            
            JsonObject json = JsonParser.parseString(payload).getAsJsonObject();

            String parametro = json.get("parametro").getAsString();
            String valore = json.get("valore").getAsString();
            
            TransizioneStato ts = d.aggiornaStato(parametro, valore);
            Evento e = new Evento("CAMBIO_STATO", d.getId(), List.of(ts));
            for (MonitorListener l : listeners) {
            	l.onEvento(e);
            }
        }
    }
}
