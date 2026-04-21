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
import domotica.domain.ParamStato;

public class ServizioReteTCPTest {

    private ServizioReteTCP servizioRete;
    private RichiestaSH richiestaFake;
    private ServerSocket fakeServer;
    private Thread serverThread;

    @BeforeEach
    public void setup() {
        servizioRete = new ServizioReteTCP();
        Comando c = new Comando("power", "ON");
        richiestaFake = new RichiestaSH("cmd", c);
    }

    @AfterEach
    public void tearDown() throws Exception {

        if (fakeServer != null && !fakeServer.isClosed()) {
        	fakeServer.close();
        }
        if (serverThread != null) {
            serverThread.interrupt();
        }
    }

    // TC-01: Controllo formato indirizzo
    @Test
    public void testSend_FormatoIndirizzoSbagliato() {
        
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> {
            servizioRete.send(richiestaFake, "192.168.1.50"); // Manca la porta
        });
        
        assertTrue(e.getMessage().contains("Formato atteso"));
    }

    @Test
    public void testSend_PortaNonNumerica() {
        
        assertThrows(NumberFormatException.class, () -> {
            servizioRete.send(richiestaFake, "192.168.1.50:abc"); 
        });
    }
    
 // TC-02: Errore dal dispositivo
    @Test
    public void testSend_RispostaError() throws Exception {

    	avviaFakeServer("{\"status\":\"ERROR\", \"msg\":\"Valore troppo alto\"}");
        
        Exception e = assertThrows(Exception.class, () -> {
            servizioRete.send(richiestaFake, "127.0.0.1:9999");
        });
        
        assertTrue(e.getMessage().contains("rifiutato il comando"), "Deve sollevare l'eccezione corretta.");
    }
    
 // TC-03: Nessun server in ascolto
    @Test
    public void testSend_NessunDispositivo() {
        // non avvio il server(la porta sarebbe comunque sbagliata)
        assertThrows(java.net.ConnectException.class, () -> {
            servizioRete.send(richiestaFake, "127.0.0.1:54321");
        }, "La connessione deve fallire");
    }

    // TC-04: Happy path
    @Test
    public void testSend_RispostaOK() throws Exception {
        
        avviaFakeServer("{\"status\":\"OK\", \"parametro\":\"temperatura\", \"valore\":\"22.5\"}");

        ParamStato risultato = servizioRete.send(richiestaFake, "127.0.0.1:9999");

        assertNotNull(risultato, "Il risultato non deve essere null");
        assertEquals("temperatura", risultato.getNome());
        assertEquals("22.5", risultato.getValore());
    }

    

    /**
     * Metodo di supporto: Crea un server locale che risponde
     * con il JSON che gli passiamo, simulando un dispositivo fisico.
     */
    private void avviaFakeServer(String jsonDiRisposta) throws Exception {
        fakeServer = new ServerSocket(9999);
        
        serverThread = new Thread(() -> {
            try {

                Socket clientSocket = fakeServer.accept();
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