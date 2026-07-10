package domotica.app;

import domotica.domain.*;
import domotica.services.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ControllerMonitoraggioTest {

    private ControllerMonitoraggio controller;
    private RegistroTargets registro;
    private Dispositivo dispositivoFinto;
    private TestMonitorListener testListener;


    class TestMonitorListener implements MonitorListener {
        public Evento lastEvent = null;
        public int eventCount = 0;

        @Override
        public void onEvento(Evento e) {
            this.lastEvent = e;
            this.eventCount++;
        }

		@Override
		public void onErrore(String errore) {
			// TODO Auto-generated method stub
		}
    }


    class ReteDiTest implements ServizioRete {
        @Override
        public void listen(MsgListener l) {
        }

		@Override
		public void send(RichiestaSH req, String dest) throws IOException {
			// TODO Auto-generated method stub
			
		}
    }


    @BeforeEach
    void setUp() {
        registro = new RegistroTargets();
        testListener = new TestMonitorListener();

        Map<String, DescParametro> mappaParam = new HashMap<>();
        mappaParam.put("power", new DescParametro("power", "string", "", false, "", "", null));
        mappaParam.put("temperatura", new DescParametro("temperatura", "double", "", false, "", "", null));
        DescDispositivo desc = new DescDispositivo("MarcaX", "Termostato", "ModelloY", mappaParam);
        
        dispositivoFinto = new Dispositivo("Termo_01", "127.0.0.1", desc);
        registro.addTarget(dispositivoFinto);

        controller = new ControllerMonitoraggio(registro, new ReteDiTest());
        controller.addMonitorListener(testListener);
        
        assertTrue(!dispositivoFinto.isOnline(), "Il dispositivo deve nascere OFFLINE di default");
        // nasce offline perchè poi viene messo automaticamente online dal servizio di monitoraggio
        // tramite un PING_SYNC
    }

    

    //TC01:testMsgRetePing
    @Test
    void testMsgRetePing() {
        dispositivoFinto.setStatoConn(false);

        //arriva un PING dalla rete
        controller.msgRete("Termo_01", "PING", "");

        assertTrue(dispositivoFinto.isOnline(), "Il dispositivo dovrebbe essere tornato ONLINE dopo il PING");
        
        // il listener ha ricevuto l evento ??
        assertEquals(1, testListener.eventCount, "Il listener doveva ricevere 1 evento");
        assertEquals("CONNESSIONE", testListener.lastEvent.getTipo());
    }

    // TC02: testMsgReteCambioStato
    @Test
    void testMsgReteCambioStato() {
        // arriva un cambio stato 
        String payloadJson = "{\"parametro\":\"power\", \"valore\":\"ON\"}";
        controller.msgRete("Termo_01", "userCmd", payloadJson);

        assertEquals("ON", dispositivoFinto.getStato().get("power"));

        // il listener ha ricevuto l evento ??
        assertEquals(1, testListener.eventCount);
        assertEquals("userCmd", testListener.lastEvent.getTipo());
        
        TransizioneStato transizione = testListener.lastEvent.getTransizioni().get(0);
        assertEquals("power", transizione.getParam());
        assertEquals("ON", transizione.getNewVal());
    }

    // TC03: testWatchdog
    @Test
    void testWatchdog() throws Exception {
    	
    	dispositivoFinto.setStatoConn(true);
    	// settiamo il last seen 15 sec fa
        dispositivoFinto.setLastSeen(System.currentTimeMillis() - 15000);

        // verificaInattività è un metodo private!
        Method metodoWatchdog = ControllerMonitoraggio.class.getDeclaredMethod("verificaInattivita");
        metodoWatchdog.setAccessible(true); 
        metodoWatchdog.invoke(controller);

        assertFalse(dispositivoFinto.isOnline(), "Il dispositivo dovrebbe essere andato OFFLINE");
        assertEquals(1, testListener.eventCount);
        assertEquals("DISCONNESSIONE", testListener.lastEvent.getTipo());
    }
    
    
    // TC04: testMsgRete_TargetSconosciuto
    @Test
    void testMsgRete_TargetSconosciuto() {
        controller.msgRete("TargetFantasma", "PING", "");
        assertEquals(0, testListener.eventCount, "Il listener non doveva registrare nessun evento");
    }

    // TC05: testMsgRete_TipoMessaggioSconosciuto
    @Test
    void testMsgRete_TipoMessaggioSconosciuto() {
        controller.msgRete("Termo_01", "TIPO_STRANO", "");
        assertEquals(0, testListener.eventCount, "Non deve generare eventi per tipi di messaggio sconosciuti");
    }

    // TC06: testAddMonitorListener_IgnoraNull
    @Test
    void testAddMonitorListener_IgnoraNull() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.addMonitorListener(null);
        }, "Il controller deve rifiutare listener nulli lanciando IllegalArgumentException");
    }
    
    // TC07: testMsgRetePingSync
    
    @Test
    void testMsgRetePingSync() {
    	String payloadJson = "{"
                + "\"idTarget\":\"" + dispositivoFinto.getId() + "\","
                + "\"tipo\":\"PING_SYNC\","
                + "\"stato\": {"
                + "    \"power\":\"ON\","
                + "    \"temperatura\":\"22.5\""
                + "},"
                + "\"indirizzo\":\"127.0.0.1:8080\"" 
                + "}";

        controller.msgRete(dispositivoFinto.getId(), "PING_SYNC", payloadJson);

        assertTrue(dispositivoFinto.isOnline(), "Il dispositivo dovrebbe essere tornato ONLINE");

        assertEquals("ON", dispositivoFinto.getStato().get("power"));
        assertEquals("22.5", dispositivoFinto.getStato().get("temperatura"));


        assertEquals(1, testListener.eventCount, "Dovrebbe essere stato generato un solo evento");
        
        Evento eventoRicevuto = testListener.lastEvent;
        assertEquals("CONNESSIONE", eventoRicevuto.getTipo());
        assertEquals(dispositivoFinto.getId(), eventoRicevuto.getIdTarget());
        
        List<TransizioneStato> transizioni = eventoRicevuto.getTransizioni();
        assertFalse(transizioni.isEmpty(), "L'evento deve contenere delle transizioni");
        
        // Sapendo che nel JSON ci sono 'power' e 'temperatura', ci aspettiamo le relative transizioni
        // (Nota: l'ordine dipende da come il tuo d.aggiornaStato inserisce/scorre la mappa, 
        // quindi controlliamo se le transizioni contengono i parametri cercati)
        boolean powerTrovato = false;
        boolean tempTrovato = false;
        for (TransizioneStato ts : transizioni) {
            if (ts.getParam().equals("power") && ts.getNewVal().equals("ON")) {
                powerTrovato = true;
            }
            if (ts.getParam().equals("temperatura") && ts.getNewVal().equals("22.5")) {
            	tempTrovato = true;
            }
            if(powerTrovato && tempTrovato) break;
        }
        assertTrue(powerTrovato, "Tra le transizioni deve esserci quella relativa al power");
    }
}