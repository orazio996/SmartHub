package domotica.services;

import domotica.domain.ParamStato;

/**
 * Interfaccia per i servizi di comunicazione verso i dispositivi.
 * Agnostico rispetto alla tecnologia sottostante.
 */
public interface ServizioRete {
    
    /**
     * Invia una richiesta a una destinazione generica e restituisce il nuovo stato.
     * @param req Il payload generato dallo Smart Hub
     * @param dest Il destinatario (es. "IP:Porta", "URL", "Topic MQTT")
     */
    ParamStato send(RichiestaSH req, String dest) throws Exception;
    
}