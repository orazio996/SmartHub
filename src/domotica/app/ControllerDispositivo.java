package domotica.app;

import domotica.domain.*;
import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Il Direttore d'Orchestra (Application Layer).
 * Coordina il Dominio e i Servizi di Rete senza contenere logica di business pura.
 */
public class ControllerDispositivo {

    // Dipendenze iniettate nel Controller
    private RegistroTargets registro;
    private Cronologia cronologia;
    private ServizioRete servizioRete;

    public ControllerDispositivo(RegistroTargets registro, Cronologia cronologia, ServizioRete servizioRete) {
        this.registro = registro;
        this.cronologia = cronologia;
        this.servizioRete = servizioRete;
    }

    /**
     * Esegue un comando su un Target.
     * Ritorna le modifiche allo stato dei dispositivi coinvolti.
     */
    public CompletableFuture<List<TransizioneStato>> eseguiComando(String parametro, String valore, String idTarget) {
        
        Target t = registro.getTarget(idTarget);
        if (t == null) {
            throw new IllegalArgumentException("Target con ID: " + idTarget + " non trovato!");
        }
        Comando comando = new Comando(parametro, valore);
        List<Dispositivo> compatibili = t.getDispositiviCompatibili(comando);

        // più thread scriveranno qui contemporaneamente
        // liste sincronizzate per supportare accessi multipli
        List<TransizioneStato> transizioni = Collections.synchronizedList(new ArrayList<>());
        
        List<CompletableFuture<Void>> tasks = new ArrayList<>();

  
        for (Dispositivo d : compatibili) {
        	
        	CompletableFuture<Void> task = CompletableFuture.runAsync(() -> {
        		
                try {
                    RichiestaSH req = new RichiestaSH("cmd", comando);
                    ParamStato nuovoParam = servizioRete.send(req, d.getIndirizzo());
                    TransizioneStato transizione = d.aggiornaStato(parametro, nuovoParam.getValore());
             
                    transizioni.add(transizione);

                } catch (Exception e) {
                    System.err.println("Errore di comunicazione col dispositivo " + d.getId() + ": " + e.getMessage());
                }
            });
            
            tasks.add(task);
        }

        return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    if (!transizioni.isEmpty()) {
                        Evento evento = new Evento("Comando " + parametro, idTarget, new ArrayList<>(transizioni));
                        cronologia.addEvento(evento);
                    }

                    return transizioni;
                });
    }
}