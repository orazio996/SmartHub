package domotica.app;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
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
    private Cronologia cronologia;


    class ReteDiTest implements ServizioRete {
        public int chiamateSend = 0;
        public RichiestaSH ultimaRichiesta = null;
        public String ultimoIndirizzo = null;
        public boolean simulaErrore = false;

        @Override
        public void send(RichiestaSH req, String dest) throws IOException{
            if (simulaErrore) {
                throw new IOException("Simulazione errore di rete");
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
        cronologia = new Cronologia();
        controller = new ControllerTargets(registro, reteTest, cronologia );
    }

    // TC01: testEseguiComando_TargetNonTrovato
    @Test
    public void testEseguiComando_TargetNonTrovato() {
        NullPointerException e = assertThrows(NullPointerException.class, () -> {
            controller.eseguiComando("power", "ON", "idInesistente", "user");
        });
        
        assertTrue(e.getMessage().contains("non trovato!"));
        assertEquals(0, reteTest.chiamateSend, "Non deve partire nessuna chiamata di rete");
    }

    // TC02: testEseguiComando_NessunDispositivoCompatibile
    @Test
    public void testEseguiComando_NessunDispositivoCompatibile() throws Exception {
        // Setup lampadina
        Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        registro.addTarget(luce);

        controller.eseguiComando("volume", "88", "LampadaScrivania", "user");
        
        assertEquals(0, reteTest.chiamateSend, "Il comando incompatibile non deve essere inviato");
    }

    // TC03: testEseguiComando_Successo
    @Test
    public void testEseguiComando_Successo() throws Exception {
        // Setup lampadina base
        Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        luce.setStatoConn(true);
        registro.addTarget(luce);

        controller.eseguiComando("power", "ON", "LampadaScrivania", "user");

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
        luce.setStatoConn(true);
        registro.addTarget(luce);

        // simulazione errore
        reteTest.simulaErrore = true; /// la cattura primaaaaaa
        Exception e = assertThrows(IllegalStateException.class, () -> {
            controller.eseguiComando("power", "ON", "LampadaScrivania", "user");
        });
        assertTrue(e.getMessage().contains("Errore rete, invio a:"));
    }
    
    // TC05: testAnnullaUltimoComando
    @Test
    public void testAnnullaUltimoComando(){
    	
    	Map<String, DescParametro> descParams = new HashMap<>();
        descParams.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        descParams.put("luminosita", new DescParametro("luminosita", "int", "%", false, "0", "100", List.of())); 
        DescDispositivo descLamp = new DescDispositivo("Philips Hue", "Lampadina", "Hue", descParams);
        Dispositivo luce = new Dispositivo("LampadaScrivania", "127.0.0.1:8080", descLamp);
        luce.setStatoConn(true);
        registro.addTarget(luce);
        
    	Evento e = newEvento(1000,1100,"power", "OFF", "ON");
    	Evento e1 = newEvento(1100,1300,"luminosita", "0", "100" );
    	cronologia.addEvento(e);
    	cronologia.addEvento(e1);
    	
    	controller.annullaUltimoComando();
    	
    	assertEquals(1, reteTest.chiamateSend, "Il comando deve essere mandato al ServizioRete");
        assertEquals("127.0.0.1:8080", reteTest.ultimoIndirizzo, "L'IP bersaglio deve essere corretto");
        assertEquals("luminosita", reteTest.ultimaRichiesta.getParam());
        assertEquals("0", reteTest.ultimaRichiesta.getVal()); 	
    	
    }
    
    /*
     * Ritorna un evento userCmd con sourceTimestamp, timestamp desiderati, param e old/newVal desiderati
     * */
    private Evento newEvento(long sourceTs, long ts, String param, String oldVal, String newVal) {
    	return new Evento("user", sourceTs, "userCmd", "LampadaScrivania", List.of(new TransizioneStato(param, oldVal, newVal, "LampadaScrivania")), ts);
    	
    }

}