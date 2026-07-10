package domotica.util;


import domotica.app.*;
import domotica.domain.RegistroTargets;
import domotica.domain.Cronologia;
import domotica.domain.MotoreRoutine;
import domotica.domain.RegistroSequenze;

public class Context {
    private final ControllerTargets controllerTargets;
    private final ControllerRoutines controllerRoutines;
    private final ControllerMetriche controllerMetriche;
    private final ControllerSequenze controllerSeq;
    private final ControllerMonitoraggio controllerMonitor;
    private final RegistroTargets registro;
    private final MotoreRoutine motoreRoutine;
    private final RegistroSequenze registroSeq;
    private final Cronologia cronologia;
    

    public Context(ControllerTargets ct, ControllerRoutines cr, ControllerMetriche cm, ControllerSequenze controllerSeq, ControllerMonitoraggio cMon, RegistroTargets reg, RegistroSequenze registroSeq, MotoreRoutine mr, Cronologia cronologia) {
        this.controllerTargets = ct;
        this.controllerRoutines = cr;
        this.controllerMetriche = cm;
        this.controllerMonitor = cMon;
        this.controllerSeq = controllerSeq;
        this.registro = reg;
        this.registroSeq = registroSeq;
        this.motoreRoutine = mr;
        this.cronologia = cronologia;
    }

    // Genera i classici Getter per tutti questi campi...
    public ControllerTargets getControllerTargets() { return controllerTargets; }
    public ControllerRoutines getControllerRoutines() { return controllerRoutines; }
    public ControllerMetriche getControllerMetriche() { return controllerMetriche; }
    public ControllerSequenze getControllerSequenze() {return controllerSeq;}
    public ControllerMonitoraggio getControllerMonitor() {return controllerMonitor;}
    public RegistroTargets getRegistro() { return registro; }
    public RegistroSequenze getRegistroSequenze() { return registroSeq; }
    public MotoreRoutine getMotoreRoutine() { return motoreRoutine; }
    public Cronologia getCronologia() { return cronologia.getCronologiaObj(); }
}