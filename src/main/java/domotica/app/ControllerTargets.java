package domotica.app;

import java.util.ArrayList;
import java.util.List;

import domotica.domain.*;
import domotica.services.ServizioRete;
import domotica.ui.dto.TargetDTO;
import domotica.util.TargetMapper;


/**
 * Il Direttore d'Orchestra (Application Layer).
 * Coordina il Dominio e i Servizi di Rete senza contenere logica di business pura.
 */
public class ControllerTargets {


    private RegistroTargets registro;
    private ServizioRete servizioRete;
    private Cronologia cronologia;

    public ControllerTargets(RegistroTargets registro, ServizioRete servizioRete, Cronologia cronologia) {
        this.registro = registro;
        this.servizioRete = servizioRete;
        this.cronologia = cronologia;
    }

    /**
     * Esegue un comando su un Target.
     */
    public void eseguiComando(String parametro, String valore, String idTarget, String source){
        ComandoSingolo comando = new ComandoSingolo(parametro, valore);
        eseguiComando(comando, idTarget, source);
    }
    
    public void eseguiComando(Comando c, String idTarget, String source){
    	Target t = registro.getTarget(idTarget);
        if (t == null) {
            throw new NullPointerException("Target con ID: " + idTarget + " non trovato!");
        }
    	c.esegui(t, servizioRete, source);
    }
    
    public void annullaUltimoComando(){
    	ArrayList<Evento> eventi = (ArrayList<Evento>)cronologia.getEventiUltimoComando();
    	
    	for(Evento e : eventi) {
    		for(TransizioneStato t : e.getTransizioni()) {
    			eseguiComando(t.getParam(), t.getOldVal(), t.getIdDispositivo(), "undo");
    		}
    	}
    	
    }
    
    /**
     * Recupera tutti i target dal dominio e li traduce in DTO per la UI.
     */
    public List<TargetDTO> getTargetsPerUI() {
        List<Target> targetsDelDominio = registro.getAllTargets();
        List<TargetDTO> listaDTO = new ArrayList<>();
        
        for (Target t : targetsDelDominio) {
            listaDTO.add(TargetMapper.creaDTO(t)); 
        }
        
        return listaDTO;
    }
    
    
}