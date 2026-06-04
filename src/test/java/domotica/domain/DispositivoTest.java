package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DispositivoTest {

    private Dispositivo dispositivo;
    private DescDispositivo desc;

    @BeforeEach
    public void setup() {

        Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON", "OFF")));
        desc = new DescDispositivo("Marca", "Tipo", "Modello", descParams);
        
        dispositivo = new Dispositivo("Dispositivo1", "192.168.1.10", desc);
    }

    // TEST 1: test copia difensiva
    @Test
    public void testGetStato_ModificaStatoIllegale() {
        dispositivo.aggiornaStato("power", "ON");

        Map<String, String> mappa = dispositivo.getStato();
        mappa.put("power", "OFF");
        mappa.put("volume", "100"); 
        
        assertEquals("ON", dispositivo.getStato().get("power"), "power deve rimanere ON");
        assertFalse(dispositivo.getStato().containsKey("volume"), "Non deve esistere il parametro volume");
    }

    // TEST 2: Primo aggiornamento stato
    @Test
    public void testAggiornaStato_PrimoAggiornamentoStato() {
        
        TransizioneStato t = dispositivo.aggiornaStato("power", "ON");
        
        assertEquals("power", t.getParam());
        assertEquals("SCONOSCIUTO", t.getOldVal(), "Valore di default: SCONOSCIUTO");
        assertEquals("ON", t.getNewVal());
        assertEquals("Dispositivo1", t.getIdDispositivo());
        
        assertEquals("ON", dispositivo.getStato().get("power"));
    }

    // TEST 3: Aggiornamento successivo
    @Test
    public void testAggiornaStato_AggiornaStato() {
        
        dispositivo.aggiornaStato("power", "OFF");
        
        TransizioneStato t = dispositivo.aggiornaStato("power", "ON");
        
        assertEquals("OFF", t.getOldVal(), "Prima era OFF");
        assertEquals("ON", t.getNewVal(), "Il nuovo valore deve essere ON");
        assertEquals("ON", dispositivo.getStato().get("power"));
    }

 // TEST 4: Comando valido
    @Test
    public void testGetDispositiviCompatibili_ComandoValido() {
        Comando comandoOk = new Comando("power", "ON");
        List<Dispositivo> compatibili = dispositivo.getDispositiviCompatibili(comandoOk);
        
        assertEquals(1, compatibili.size());
        assertEquals(dispositivo, compatibili.get(0));
    }

    // TEST 5: Comando non valido
    @Test
    public void testGetDispositiviCompatibili_ComandoInvalido() {
        Comando comandoErrato = new Comando("volume", "88");
        List<Dispositivo> compatibili = dispositivo.getDispositiviCompatibili(comandoErrato);
        
        assertTrue(compatibili.isEmpty());
    }

    // TEST 6: parametro stato inesistente
    @Test
    public void testAggiornaStato_ParametroInesistente() {
        
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            dispositivo.aggiornaStato("temperatura", "22.5");
        });
        
        assertTrue(e.getMessage().contains("non fa parte"));
    }
}