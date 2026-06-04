package domotica.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import domotica.app.ControllerTargets;

public class MotoreRoutine implements MonitorListener {

    private List<Routine> routines;
    private ControllerTargets ct;

    private ScheduledExecutorService scheduler;
    public MotoreRoutine(ControllerTargets ct) {
        this.routines = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(4);
        this.ct = ct;
    }

    /**
     * Aggiunge una routine al motore. Se è temporale, fa partire subito il timer.
     */
    public void addRoutine(Routine r) {
        if (r == null) return;
        
        routines.add(r);

        if (r.getTrigger() instanceof TriggerTemporale) {
            schedule(r);
        }
    }

    
    
    /**
     * Routine temporale: Metodo ricorsivo che programma le esecuzioni future di una routine temporale.
     */
    private void schedule(Routine r) {
        TriggerTemporale trigger = (TriggerTemporale) r.getTrigger();

        long delay = trigger.getDelay();
        
        scheduler.schedule(() -> {
            if (r.isAbilitata()) {
            	ct.eseguiComando(r.getComando().getParam(), r.getComando().getValore(), r.getTarget().getId());
            }
            
            // scheduling ricorsivo
            schedule(r);
            
        }, delay, TimeUnit.MILLISECONDS);
    }

    /**
     * Routine ad eventi: scatta in automatico ogni volta che il ControllerMonitoraggio segnala una transizione di stato.
     */
    @Override
    public void onEvento(Evento e) {
        if(e.getTipo().equals("CAMBIO_STATO")) {
        	for (Routine r : routines) {
        		for (TransizioneStato ts : e.getTransizioni()) {
	                if (r.isAbilitata() && r.getTrigger().isSoddisfatto(ts)) {
	                    ct.eseguiComando(r.getComando().getParam(), r.getComando().getValore(), r.getTarget().getId());
	                }
        		}
            }
        }
    }


    /**
     * Metodo per spegnere il motore in modo pulito 
     */
    public void shutdown() {
        scheduler.shutdown();
    }

    public List<Routine> getRoutines() {
        return routines;
    }
}