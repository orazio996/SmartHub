package domotica.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

public class CalcolatoreUptime extends CalcolatoreMetrica {

    public CalcolatoreUptime(TipoMetrica tipoMetrica) {
        super(tipoMetrica);
    }

    
    
    /**
     * Calcola l'uptime in ore.
     * Se il target t corrisponde ad un gruppo di 
     * dispositivi restituisce l'uptime medio.
     */
    @Override
    protected double calcola(Target t, String param, List<Evento> eventi, FiltroTemporale filtro) {
        if (eventi == null || eventi.isEmpty()) {
            return 0.0;
        }
        double uptimeTot = 0.0;
        
        // metodo polimorfico
        List<Dispositivo> dispositivi = t.getDispositivi();

        for (Dispositivo d : dispositivi) {
        	uptimeTot += calcolaUptimeSingolo(d, eventi, filtro);
        }
        if(t instanceof Gruppo) {
        	return uptimeTot/dispositivi.size();
        }
        return uptimeTot;
    }
    
    

    /**
     * Calcola l'uptime di ciascun dispositivo in base agli eventi in cronologia.
     */
    private double calcolaUptimeSingolo(Dispositivo d, List<Evento> eventi, FiltroTemporale f) {
        double uptime = 0.0;
        long orarioUltimoON = 0;
        String statoPower;

        for (Evento e : eventi) {
        	
        	if(e.getIdTarget().equals(d.getId())) {
	        	statoPower = e.getTransizioni().getFirst().getNewVal();
	        	
	        	if ("CONNESSIONE".equalsIgnoreCase(e.getTipo()) && statoPower.equalsIgnoreCase("ON")) {
	                orarioUltimoON = e.getTimestamp();
	                continue;
	            } else if ("DISCONNESSIONE".equalsIgnoreCase(e.getTipo())) {
	            	if(orarioUltimoON != 0) {
	            		long msAcceso = e.getTimestamp() - orarioUltimoON;
		                long secondiAcceso = msAcceso/1000;
		                uptime += secondiAcceso / 3600.0;
		                
		                orarioUltimoON = 0;
	            	}    
	                continue;
	            } else if (e.getTipo().contains("Cmd")) {
	                for (TransizioneStato transizione : e.getTransizioni()) {
	                        
	                    if ("power".equalsIgnoreCase(transizione.getParam())) {
	                        
	                        if ("ON".equalsIgnoreCase(transizione.getNewVal())) {
	                            if (orarioUltimoON == 0) {
	                                orarioUltimoON = e.getTimestamp();
	                            }
	                            
	                        } else if ("OFF".equalsIgnoreCase(transizione.getNewVal())) {
	                            if (orarioUltimoON != 0) {
	                            	long msAcceso = e.getTimestamp() - orarioUltimoON;
	                                long secondiAcceso = msAcceso/1000;
	                                uptime += secondiAcceso / 3600.0;
	                                
	                                orarioUltimoON = 0;
	                            }
	                        }
	                    }
	                }
	            }
        	}
        }
        
        if (orarioUltimoON != 0) {
        	LocalDateTime dataOraFine;
        	if(f.getDataFine().equals(LocalDate.now())) {
        		dataOraFine = f.getDataFine().atTime(LocalTime.now());
        	} else {
        		dataOraFine = f.getDataFine().atTime(23, 59, 59);
        	}
        	
        	long dataOraFineMs = dataOraFine.atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        	long msAcceso = dataOraFineMs - orarioUltimoON;
            long secondiAcceso = msAcceso/1000;
            uptime += secondiAcceso / 3600.0;
        }

        return uptime;
    }

    @Override
    protected String getUnitaMisura() {
        return "h";
    }
}