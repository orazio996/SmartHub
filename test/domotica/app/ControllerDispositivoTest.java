package domotica.app;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domotica.domain.*;
import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

public class ControllerDispositivoTest {

    private RegistroTargets registro;
    private Cronologia cronologia;
    private ControllerDispositivo controller;
    
    // FInto Servizio Rete per simulare una connessione di rete durante i test!
    private ServizioRete reteFinta = new ServizioRete() {
        @Override
        public ParamStato send(RichiestaSH req, String indirizzo) {
            return new ParamStato(req.getParam(), req.getVal());
        }
    };

    @BeforeEach
    public void setup() {
        registro = new RegistroTargets();
        cronologia = new Cronologia();
        controller = new ControllerDispositivo(registro, cronologia, reteFinta);
    }

    // TC-01: Target Inesistente
    @Test
    public void testEseguiComando_TargetNonTrovato() {
    	
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            controller.eseguiComando("power", "ON", "idInesistente");
        });
        
        assertTrue(e.getMessage().contains("non trovato!"));
    }

    // TC-02: Comando non compatibile
    @Test
    public void testEseguiComando_NessunDispositivoCompatibile() throws Exception {
        
    	// setup lampadina
        Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("luminosita", new DescParametro("luminosita", "int", "%", false, "0", "100", List.of())); 
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        descParams.put("colore", new DescParametro("colore", "string", "", false, "", "", List.of("Rosso", "Verde", "Blu"))); 
        descParams.put("lampeggio", new DescParametro("lampeggio", "float", "sec", false, "0.2", "3s", List.of())); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue White 9W", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        registro.addTarget(luce);
        
        // Comando a caso
        // .join() per aspettare il CompletableFuture
        List<TransizioneStato> risultato = controller.eseguiComando("volume", "88", "LampadaScrivania").join();
        
        // non ci dovrebbero essere transizioni e di conseguenza la cronologia rimane vuota
        assertTrue(risultato.isEmpty(), "Non dovrebbero esserci transizioni per comandi incompatibili");
        assertEquals(0, cronologia.getCronologia().size(), "La cronologia deve restare vuota");
    }

    // TC-03: Happy path
    @Test
    public void testEseguiComando_Successo() throws Exception {

    	// setup lampadina
    	Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("luminosita", new DescParametro("luminosita", "int", "%", false, "0", "100", List.of())); 
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        descParams.put("colore", new DescParametro("colore", "string", "", false, "", "", List.of("Rosso", "Verde", "Blu"))); 
        descParams.put("lampeggio", new DescParametro("lampeggio", "float", "sec", false, "0.2", "3s", List.of())); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue White 9W", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        luce.aggiornaStato("power", "OFF");
        registro.addTarget(luce);

        // comando valido
        List<TransizioneStato> risultato = controller.eseguiComando("power", "ON", "LampadaScrivania").join();
        
        assertEquals(1, risultato.size(), "Deve esserci una sola transizione");
        
        assertEquals("ON", luce.getStato().get("power"), "Lo stato della luce deve essere ON");

        assertEquals(1, cronologia.getCronologia().size(), "Salvato un evento in cronologia");
        Evento salvato = cronologia.getCronologia().get(0);
        assertEquals("Comando power", salvato.getTipo(), "Il tipo dell'evento deve essere corretto");
    }
}