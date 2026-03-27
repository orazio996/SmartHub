package domotica.app;

import domotica.domain.*;
import domotica.services.RichiestaIPC;
import domotica.services.ServizioIPC;

import java.util.ArrayList;
import java.util.List;

/**
 * Il Direttore d'Orchestra (Application Layer).
 * Coordina il Dominio e i Servizi di Rete senza contenere logica di business pura.
 */
public class ControllerDispositivo {

    // Dipendenze iniettate nel Controller
    private RegistroTargets registro;
    private Cronologia cronologia;
    private ServizioIPC servizioRete;

    public ControllerDispositivo(RegistroTargets registro, Cronologia cronologia, ServizioIPC servizioRete) {
        this.registro = registro;
        this.cronologia = cronologia;
        this.servizioRete = servizioRete;
    }

    /**
     * Esegue un comando su un Target.
     * Ritorna le modifiche allo stato dei dispositivi coinvolti.
     */
    public List<ParamStato> eseguiComando(String parametro, String valore, String idTarget) {
        
        Target t = registro.getTarget(idTarget);
        if (t == null) {
            throw new IllegalArgumentException("Target con ID: " + idTarget + " non trovato!");
        }

        Comando comando = new Comando(parametro, valore);
        List<Dispositivo> compatibili = t.getDispositiviCompatibili(comando);

        List<TransizioneStato> transizioni = new ArrayList<>();
        List<ParamStato> paramsAggiornati = new ArrayList<>();

  
        for (Dispositivo d : compatibili) {
            try {
                RichiestaIPC req = new RichiestaIPC("cmd", comando);

                // bloccante
                ParamStato nuovoParam = servizioRete.send(req, d.getIndirizzo(), d.getPorta());

                TransizioneStato transizione = d.aggiornaStato(parametro, nuovoParam.getValore());
         
                transizioni.add(transizione);
                paramsAggiornati.add(nuovoParam);

            } catch (Exception e) {
                // Se una luce del gruppo è offline, catturo l'errore
                // ma il ciclo continua per accendere le altre luci
                System.err.println("Errore di comunicazione col dispositivo " + d.getId() + ": " + e.getMessage());
            }
        }

        if (!transizioni.isEmpty()) {
            Evento evento = new Evento("Comando " + parametro, idTarget, transizioni);
            cronologia.addEvento(evento);
        }

        return paramsAggiornati;
    }
}