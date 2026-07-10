package domotica.util;

public interface ServizioStartup {
    /**
     * Legge le configurazioni, innesca i servizi e restituisce il contesto pronto.
     */
    Context avviaSistema() throws Exception;
}
