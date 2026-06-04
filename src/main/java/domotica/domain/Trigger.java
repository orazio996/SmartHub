package domotica.domain;

public abstract class Trigger {
    

    /**
     * Valuta se l'evento passato soddisfa le condizioni del trigger.
     */
    public abstract boolean isSoddisfatto(TransizioneStato ts);
}