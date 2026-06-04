package domotica.app;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domotica.domain.*;
import domotica.services.MsgListener;
import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

public class ControllerTargetsTest {

    private RegistroTargets registro;
    private ControllerTargets controller;
    private ReteDiTest reteTest;


    class ReteDiTest implements ServizioRete {
        public int chiamateSend = 0;
        public RichiestaSH ultimaRichiesta = null;
        public String ultimoIndirizzo = null;
        public boolean simulaErrore = false;

        @Override
        public void send(RichiestaSH req, String dest) throws Exception {
            if (simulaErrore) {
                throw new Exception("Simulazione errore di rete");
            }
            this.chiamateSend++;
            this.ultimaRichiesta = req;
            this.ultimoIndirizzo = dest;
        }

        @Override
        public void listen(MsgListener listener) {
        }
    }

    @BeforeEach
    public void setup() {
        registro = new RegistroTargets();
        reteTest = new ReteDiTest();
        controller = new ControllerTargets(registro, reteTest);
    }

    // TC01: testEseguiComando_TargetNonTrovato
    @Test
    public void testEseguiComando_TargetNonTrovato() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            controller.eseguiComando("power", "ON", "idInesistente");
        });
        
        assertTrue(e.getMessage().contains("non trovato!"));
        assertEquals(0, reteTest.chiamateSend, "Non deve partire nessuna chiamata di rete");
    }

    // TC02: testEseguiComando_NessunDispositivoCompatibile
    @Test
    public void testEseguiComando_NessunDispositivoCompatibile() {
        // Setup lampadina
        Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        registro.addTarget(luce);

        controller.eseguiComando("volume", "88", "LampadaScrivania");
        
        assertEquals(0, reteTest.chiamateSend, "Il comando incompatibile non deve essere inviato");
    }

    // TC03: testEseguiComando_Successo
    @Test
    public void testEseguiComando_Successo() {
        // Setup lampadina base
        Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        registro.addTarget(luce);

        controller.eseguiComando("power", "ON", "LampadaScrivania");

        assertEquals(1, reteTest.chiamateSend, "Il comando deve essere mandato al ServizioRete");
        assertEquals("127.0.0.1:8080", reteTest.ultimoIndirizzo, "L'IP bersaglio deve essere corretto");
        assertEquals("power", reteTest.ultimaRichiesta.getParam());
        assertEquals("ON", reteTest.ultimaRichiesta.getVal());
    }

    // TC04: testEseguiComando_ErroreDiComunicazione
    @Test
    public void testEseguiComando_ErroreDiComunicazione() {
        Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        registro.addTarget(luce);

        // simulazione errore
        reteTest.simulaErrore = true;

        assertDoesNotThrow(() -> {
            controller.eseguiComando("power", "ON", "LampadaScrivania");
        }, "L'eccezione deve essere catturata.");
    }
}