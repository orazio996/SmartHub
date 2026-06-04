package domotica.app;

import domotica.domain.*;
import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

import java.util.List;

/**
 * Il Direttore d'Orchestra (Application Layer).
 * Coordina il Dominio e i Servizi di Rete senza contenere logica di business pura.
 */
public class ControllerTargets {

    // Dipendenze iniettate nel Controller
    private RegistroTargets registro;
    private ServizioRete servizioRete;

    public ControllerTargets(RegistroTargets registro, ServizioRete servizioRete) {
        this.registro = registro;
        this.servizioRete = servizioRete;
    }

    /**
     * Esegue un comando su un Target.
     */
    public void eseguiComando(String parametro, String valore, String idTarget) {
        
        Target t = registro.getTarget(idTarget);
        if (t == null) {
            throw new IllegalArgumentException("Target con ID: " + idTarget + " non trovato!");
        }
        Comando comando = new Comando(parametro, valore);
        List<Dispositivo> compatibili = t.getDispositiviCompatibili(comando);
        
        for (Dispositivo d : compatibili) {
        	try {
              RichiestaSH req = new RichiestaSH("cmd", comando);
              servizioRete.send(req, d.getIndirizzo());

          } catch (Exception e) {
              System.err.println("Errore di comunicazione col dispositivo " + d.getId() + ": " + e.getMessage());
          }
        }
      
    }
}