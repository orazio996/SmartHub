package domotica.domain;

import java.util.List;

public class CalcolatoreMinMax extends CalcolatoreMetrica {

    private final boolean isMassimo;
    private String unitaMisura;

    public CalcolatoreMinMax(TipoMetrica tipoMetrica) {
        super(tipoMetrica);

        String nomeTipo = tipoMetrica.getNome().toUpperCase();
        if (nomeTipo.contains("MAX")){
            this.isMassimo = true;
        } else if (nomeTipo.contains("MIN")){
            this.isMassimo = false;
        } else {
            throw new IllegalArgumentException("Tipo metrica non supportato: " + tipoMetrica.getNome());
        }
    }

    @Override
    protected double calcola(Target target, String param, List<Evento> eventi, FiltroTemporale filtro) {
        
    	
    	for(Dispositivo d : target.getDispositivi()) {
    		if(d.getDescrizione().getDescParametri().containsKey(param)) {
    			this.unitaMisura = d.getDescrizione().getDescParametri().get(param).getUnitaMisura();
    			break;
    		}
    	}
    	
    	if (eventi == null || eventi.isEmpty()) {
        	throw new IllegalArgumentException("Dati insufficenti per calcolare: " + tipoMetrica.getNome());
        }

        double valoreTrovato = isMassimo ? -Double.MAX_VALUE : Double.MAX_VALUE;
        boolean almenoUnDatoValido = false;

        for (Evento e : eventi) {

            if (!e.getIdTarget().equals(target.getId())) {
                continue;
            }

            if (e.getTransizioni() != null) {
                for (TransizioneStato ts : e.getTransizioni()) {
                    if (ts.getParam().equals(param)) {
                        try {
                            double val = Double.parseDouble(ts.getNewVal());
                            
                            if (isMassimo) {
                                if (val > valoreTrovato) valoreTrovato = val;
                            } else {
                                if (val < valoreTrovato) valoreTrovato = val;
                            }
                            almenoUnDatoValido = true;

                        } catch (NumberFormatException ex) {
                        	throw new IllegalArgumentException("Tipo parametro non numerico: " + param);
                        }
                    }
                }
            }
        }

        if (almenoUnDatoValido) {
        	return valoreTrovato;
        } else {
        	throw new IllegalArgumentException("Dati insufficenti per calcolare: " + tipoMetrica.getNome());
        }
    }

    @Override
    protected String getUnitaMisura() {
        return unitaMisura;
    }
}