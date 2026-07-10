package domotica.domain;

import domotica.services.ServizioRete;

public abstract class Comando {
	
	public void esegui(Target t, ServizioRete rete, String source){
		long sourceTimestamp = System.currentTimeMillis();
		esegui(t, rete, source, sourceTimestamp);
	}
	
	public abstract void esegui(Target t, ServizioRete rete, String source, long sourceTimestamp);
}


