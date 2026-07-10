package domotica.app;

import domotica.domain.*;
import domotica.services.*;

import java.util.Objects;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


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
                
                String param;
                String value;
                if (d.getStato().containsKey("power")) {
                	param = "power";
                	value = d.getStato().get("power");
                } else {
                	param = "statoConn";
                	value = "OFF";
                }
                	TransizioneStato transizione=new TransizioneStato(param, value, value, d.getId());
                
                Evento e = new Evento("DISCONNESSIONE", d.getId(), List.of(transizione));
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

        try {
        	if (tipo.equals("PING")) {
//              System.out.println("[RETE] Ping ricevuto da " + idTarget);
              d.setLastSeen(oraAttuale);
              
              // Se era offline, lo rimettiamo online
              if (!d.isOnline()) {
              	
                  d.setStatoConn(true);
                  System.out.println("[PING] Dispositivo " + d.getId() + " è ONLINE.");
                  
                  String param;
                  String value;
                  if (d.getStato().containsKey("power")) {
                  	param = "power";
                  	value = d.getStato().get("power");
                  } else {
                  	param = "statoConn";
                  	value = "ON";
                  }
                  
              	TransizioneStato transizione=new TransizioneStato(param, value, value, d.getId());
                  Evento e = new Evento("CONNESSIONE", d.getId(), List.of(transizione));
                  for (MonitorListener l : listeners) {
                  	l.onEvento(e);
                  }
                  
              }

          } else if (tipo.equals("PING_SYNC")) {
          	d.setLastSeen(oraAttuale);
              if (!d.isOnline()) {
                  d.setStatoConn(true);
                  System.out.println("[PING] Dispositivo " + d.getId() + " è ONLINE.");
                  ArrayList<TransizioneStato> transizioni = new ArrayList<TransizioneStato>();
                  JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
                  JsonObject stato = json.getAsJsonObject("stato");
                  
                  if(!stato.has("power")) {
                	  TransizioneStato ts = new TransizioneStato("statoConn", "ON", "ON", d.getId());
                	  transizioni.add(ts);
                  }

                  for (Map.Entry<String, JsonElement> entry : stato.entrySet()) {
                  	TransizioneStato ts = d.aggiornaStato(entry.getKey(), entry.getValue().getAsString());
                  	if(ts!=null) transizioni.add(ts);
                  }
                  try {
  					servizioRete.send(new RichiestaSH("system","system", "PING_ACK", System.currentTimeMillis(), new ComandoSingolo("statoConn","ONLINE")), json.get("indirizzo").getAsString());
  				} catch (IOException e) {
  					System.err.println("[RETE] Impossibile inviare PING_ACK a " + d.getIndirizzo() + ": " + e.getMessage());
  				}
                  Evento e = new Evento("CONNESSIONE", d.getId(), transizioni);
                  for (MonitorListener l : listeners) {
                  	l.onEvento(e);
                  }
                  
              }	
          
          } else if (tipo.contains("Cmd")) {
              System.out.println("[RETE] Cambio stato per " + idTarget + ": " + payload);
              d.setLastSeen(oraAttuale); // Un cambio stato vale anche come segno di vita
              
              JsonObject json = JsonParser.parseString(payload).getAsJsonObject();
              
              String sourceTarget = json.has("sourceTarget") ? json.get("sourceTarget").getAsString() : "sconosciuto";
              String source = json.has("source") ? json.get("source").getAsString() : "sconosciuto";
              String sourceTimestamp = json.has("sourceTimestamp") ? json.get("sourceTimestamp").getAsString() : "0";
              String parametro = json.has("parametro") ? json.get("parametro").getAsString() : "sconosciuto";
              String valore = json.has("valore") ? json.get("valore").getAsString() : "sconosciuto";
              
              TransizioneStato ts = d.aggiornaStato(parametro, valore);
              if (ts != null) {
  	            Evento e = new Evento(source, Long.parseLong(sourceTimestamp), tipo, sourceTarget, List.of(ts));
  	            for (MonitorListener l : listeners) {
  	            	l.onEvento(e);
  	            }
          	}
          }
        } catch (JsonSyntaxException | IllegalStateException | NumberFormatException e) {
            System.err.println("Errore di formato nel payload da " + idTarget + ": " + e.getMessage());
        }
    }
    
    public void msgErrore(String errore) {
    	for (MonitorListener l : listeners) {
        	l.onErrore(errore);
        }
    }
}
