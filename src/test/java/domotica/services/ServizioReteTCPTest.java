package domotica.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domotica.domain.Comando;

public class ServizioReteTCPTest {

    private ServizioReteTCP servizioRete;
    private RichiestaSH testReq;
    private ServerSocket testServer;
    private Thread serverThread;
    private TestListener listener;
    private static int testPort = 6000;
    
    class TestListener implements domotica.services.MsgListener {
        String id = null;
        String tipo = null;
        int eventCount = 0;

        @Override
        public void msgRete(String idTarget, String tipo, String payload) {
            this.id = idTarget;
            this.tipo = tipo;
            this.eventCount++;
        }
    }

    @BeforeEach
    public void setup() {
    	testPort++;
        servizioRete = new ServizioReteTCP(testPort); 
        listener = new TestListener();
        
        Comando c = new Comando("power", "ON");
        testReq = new RichiestaSH("cmd", c);
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (testServer != null && !testServer.isClosed()) {
        	testServer.close();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    // TC-01: Controllo formato indirizzo
    @Test
    public void testSend_FormatoIndirizzoSbagliato() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            servizioRete.send(testReq, "192.168.1.50"); // Manca la porta
        });
        assertTrue(e.getMessage().contains("Formato atteso"));
    }

    
    
    // TC-02: testSend_RispostaError
    @Test
    public void testSend_RispostaError() throws Exception {
    	
        avviaTestServer("{\"status\":\"ERROR\", \"msg\":\"Valore troppo alto\"}");
        
        Exception e = assertThrows(Exception.class, () -> {
            servizioRete.send(testReq, "127.0.0.1:9999");
        });

        assertTrue(e.getMessage().contains("rifiutato dal dispositivo"));
        assertTrue(e.getMessage().contains("Valore troppo alto"));
    }
    
    // TC-03: Nessun server in ascolto
    @Test
    public void testSend_NessunDispositivo() {
        assertThrows(java.net.ConnectException.class, () -> {
            servizioRete.send(testReq, "127.0.0.1:54321");
        }, "La connessione deve fallire");
    }

    // TC-04: testSend_RispostaOK
    @Test
    public void testSend_RispostaOK() throws Exception {
    	avviaTestServer("{\"status\":\"OK\", \"parametro\":\"temperatura\", \"valore\":\"22.5\"}");

        assertDoesNotThrow(() -> {
            servizioRete.send(testReq, "127.0.0.1:9999");
        }, "Tutto OK nessuna eccezione dovrebbe essere generata.");
    }
    
    
    // TC-05: testListen_RicezioneMessaggio
    @Test
    public void testListen_RicezioneMessaggio() throws Exception {
        servizioRete.listen(listener);
        Thread.sleep(200); 
        
        try (Socket lampadinaFinta = new Socket("127.0.0.1", testPort);
             PrintWriter out = new PrintWriter(lampadinaFinta.getOutputStream(), true)) {
            out.println("{\"idTarget\":\"LampadaSalotto\", \"tipo\":\"PING\"}");
        }
        Thread.sleep(200); 
        
        assertEquals("LampadaSalotto", listener.id);
        assertEquals("PING", listener.tipo);
        assertEquals(1, listener.eventCount);
    }

    // TC-06: testListen_JsonMalformato
    @Test
    public void testListen_JsonMalformato() throws Exception {
        servizioRete.listen(listener);
        Thread.sleep(200); 

        try (Socket lampadinaFinta = new Socket("127.0.0.1", testPort);
             PrintWriter out = new PrintWriter(lampadinaFinta.getOutputStream(), true)) {
            out.println("messaggio corrotto");
        }
        Thread.sleep(200); 
        assertEquals(0, listener.eventCount);
        
        // il server continua a funzionare a seguito di msg non valido
        try (Socket lampadinaFinta = new Socket("127.0.0.1", testPort);
             PrintWriter out = new PrintWriter(lampadinaFinta.getOutputStream(), true)) {
            out.println("{\"idTarget\":\"Luce1\", \"tipo\":\"PING\"}");
        }
        Thread.sleep(200); 
        assertEquals(1, listener.eventCount);
    }

    // TC-07: testListen_CampiMancanti
    @Test
    public void testListen_CampiMancanti() throws Exception {
        servizioRete.listen(listener);
        Thread.sleep(200); 
        
        try (Socket lampadinaFinta = new Socket("127.0.0.1", testPort);
             PrintWriter out = new PrintWriter(lampadinaFinta.getOutputStream(), true)) {
            out.println("{\"paramInutile\":\"1234\"}");
        }
        Thread.sleep(200); 
        
        assertEquals("UNKNOWN", listener.id);
        assertEquals("UNKNOWN", listener.tipo);
        
     // il server continua a funzionare a seguito di msg non valido
        try (Socket lampadinaFinta = new Socket("127.0.0.1", testPort);
             PrintWriter out = new PrintWriter(lampadinaFinta.getOutputStream(), true)) {
            out.println("{\"idTarget\":\"Luce1\", \"tipo\":\"PING\"}");
        }
        Thread.sleep(200); 
        assertEquals(2, listener.eventCount);
    }
    
    
 // TC08: testSend_PortaNonNumerica
    @Test
    public void testSend_PortaNonNumerica() {
        assertThrows(NumberFormatException.class, () -> {
            servizioRete.send(testReq, "192.168.1.50:abc"); 
        });
    }
    
    

    /**
     * Metodo di supporto: Crea un server locale che risponde
     * con il JSON che gli passiamo, simulando un dispositivo fisico.
     */
    private void avviaTestServer(String jsonDiRisposta) throws Exception {
    	testServer = new ServerSocket(9999);
        
        serverThread = new Thread(() -> {
            try {
                Socket clientSocket = testServer.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                
                in.readLine();

                out.println(jsonDiRisposta);
                
                clientSocket.close();
            } catch (Exception e) {
            }
        });
        serverThread.start();
    }
}