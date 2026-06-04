package domotica.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domotica.app.ControllerMonitoraggio;
import domotica.app.ControllerRoutines;
import domotica.app.ControllerTargets;
import domotica.domain.Cronologia;
import domotica.domain.DescDispositivo;
import domotica.domain.DescParametro;
import domotica.domain.Dispositivo;
import domotica.domain.MotoreRoutine;
import domotica.domain.RegistroTargets;
import domotica.services.ServizioReteTCP;

public class SystemIntegrationTest {


    private RegistroTargets registro;
    private Cronologia cronologia;
    private ServizioReteTCP servizioRete;
    private ControllerTargets controllerTargets;
    private MotoreRoutine motoreRoutine;
    private ControllerMonitoraggio controllerMonitor;
    private ControllerRoutines controllerRoutines;

    private Dispositivo lampada;
    private Dispositivo termostato;
    private Dispositivo sensore;

    private static int hubPort = 5000;
    private static int devicePort = 8080;

    @BeforeEach
    public void avviaHub() {
        hubPort++;
        devicePort++;

        registro = new RegistroTargets();
        cronologia = new Cronologia();
        servizioRete = new ServizioReteTCP(hubPort);

        controllerTargets = new ControllerTargets(registro, servizioRete);
        motoreRoutine = new MotoreRoutine(controllerTargets);
        
        controllerMonitor = new ControllerMonitoraggio(registro, servizioRete);
        controllerMonitor.addMonitorListener(cronologia);
        controllerMonitor.addMonitorListener(motoreRoutine);
        
        controllerRoutines = new ControllerRoutines(motoreRoutine, registro);


        Map<String, DescParametro> paramsLamp = new HashMap<>();
        paramsLamp.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON", "OFF")));
        DescDispositivo descLamp = new DescDispositivo("Philips", "Luce", "Hue", paramsLamp);
        lampada = new Dispositivo("LampadaSalotto", "127.0.0.1:" + devicePort, descLamp);
        

        Map<String, DescParametro> paramsTerm = new HashMap<>();
        paramsTerm.put("power", new DescParametro("power", "string", "", false, "", "", List.of("ON", "OFF")));
        DescDispositivo descTerm = new DescDispositivo("Samsung", "Termostato", "CaldoMax", paramsTerm);
        termostato = new Dispositivo("Termostato", "127.0.0.1:" + + (devicePort + 1), descTerm);
        
        
        Map<String, DescParametro> paramsSens = new HashMap<>();
        paramsSens.put("temperatura", new DescParametro("temperatura", "float", "C", true, "0", "50", List.of()));
        DescDispositivo descSens = new DescDispositivo("Samsung", "Sensore", "Temp", paramsSens);
        sensore = new Dispositivo("SensoreTemp", "127.0.0.1:" + (devicePort + 2), descSens);

        registro.addTarget(lampada);
        registro.addTarget(termostato);
        registro.addTarget(sensore);

        // Avvio dei Thread di ascolto
        controllerMonitor.start();
    }

    @AfterEach
    public void spegniHub() {
        motoreRoutine.shutdown();
    }


    // UC02: Comanda Dispositivi 
    @Test
    public void testUC02_ComandaDispositivo() throws Exception {
        CompletableFuture<String> msg = new CompletableFuture<>();

        // dispositivo di test che aspetta msg
        Thread hardwareFisico = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(devicePort);
                 Socket client = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
            	msg.complete(in.readLine());
            } catch (Exception e) {
            	msg.completeExceptionally(e);
            }
        });
        hardwareFisico.start();
        Thread.sleep(200);

        // eseguo il comando
        controllerTargets.eseguiComando("power", "ON", "LampadaSalotto");

        // ne verifico l effetto
        String payload = msg.get(3, TimeUnit.SECONDS);
        assertNotNull(payload, "Il comando non è mai uscito dalla rete");
        assertTrue(payload.contains("power"), "Manca il parametro");
        assertTrue(payload.contains("ON"), "Manca il valore");
    }


    // UC06a: Crea Routine
    @Test
    public void testUC06a_CreaRoutine() {
        String cmdJson = "{\"param\": \"power\", \"valore\": \"ON\"}";
        String triggerJson = "{\"targetOsservato\": \"SensoreTemp\", \"paramOsservato\": \"temperatura\", \"operatore\": \"<\", \"soglia\": \"18\"}";
        
        controllerRoutines.addRoutine("Riscaldamento", "EVENT", "Termostato", cmdJson, triggerJson);

        assertEquals(1, motoreRoutine.getRoutines().size(), "La routine non è stata salvata");
        assertEquals("Riscaldamento", motoreRoutine.getRoutines().get(0).getNome());
        assertEquals("Termostato", motoreRoutine.getRoutines().get(0).getTarget().getId());
    }
    
 // UC06b: Attiva Routine (Event-Driven)
    @Test
    public void testUC06b_EsecuzioneRoutine_EventDriven() throws Exception {
        
        CompletableFuture<String> msgRicevuto = new CompletableFuture<>();
        // questo è il termostato
        Thread temostato = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(devicePort + 1);
                 Socket client = server.accept();
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {
                
            	msgRicevuto.complete(in.readLine());
                
            } catch (Exception e) {
            	msgRicevuto.completeExceptionally(e);
            }
        });
        temostato.start();
        Thread.sleep(200);
        // preparo la routine
        String cmdJson = "{\"param\": \"power\", \"valore\": \"ON\"}";
        String triggerJson = "{\"targetOsservato\": \"SensoreTemp\", \"paramOsservato\": \"temperatura\", \"operatore\": \"<\", \"soglia\": \"18\"}";
        controllerRoutines.addRoutine("RiscaldamentoAuto", "EVENT", "Termostato", cmdJson, triggerJson);

        // simulo l arrivo del messaggio dal sensore
        String msgSensore = "{\"idTarget\":\"SensoreTemp\", \"tipo\":\"CAMBIO_STATO\", \"parametro\":\"temperatura\", \"valore\":\"16.0\"}";        try (Socket socketVersoHub = new Socket("127.0.0.1", hubPort);
             PrintWriter out = new PrintWriter(socketVersoHub.getOutputStream(), true)) {
            out.println(msgSensore);
        }

        // verifico l'invio del comando al dispositivo target
        String payload = msgRicevuto.get(4, TimeUnit.SECONDS);
        
        assertNotNull(payload, "Il termostato non ha ricevuto nessun comando: la routine non è scattata!");
        assertTrue(payload.contains("power") && payload.contains("ON"), "La routine ha inviato il comando sbagliato");
    }


    // UC07: Monitoraggio: msgRete()
    @Test
    public void testUC07_Monitoraggio_MsgRete() throws Exception {
    	
        assertNotEquals("ON", lampada.getStato().get("power"));
        int eventiIniziali = cronologia.getCronologia().size();

        
        String msg = "{\"idTarget\":\"LampadaSalotto\", \"tipo\":\"CAMBIO_STATO\", \"parametro\":\"power\", \"valore\":\"ON\"}";
        // dispositivo di test che manda msg
        try (Socket socketVersoHub = new Socket("127.0.0.1", hubPort);
             PrintWriter out = new PrintWriter(socketVersoHub.getOutputStream(), true)) {
            out.println(msg);
        }

        Thread.sleep(500);

        assertEquals("ON", lampada.getStato().get("power"), "Lo stato del dispositivo non è stato aggiornato");
        assertEquals(eventiIniziali + 1, cronologia.getCronologia().size(), "La cronologia non e stata aggiornata");
        assertEquals("CAMBIO_STATO", cronologia.getCronologia().get(eventiIniziali).getTipo(), "Tipo sbagliato");
    }


    // UC07 Monitoraggio: verificaInattivita()
    @Test
    public void testUC07_Monitoraggio_VerificaInattivita() throws Exception {

        assertTrue(lampada.isOnline());

        // impostiamo lastSeen a 20 sec fa
        lampada.setLastSeen(System.currentTimeMillis() - 20000);


        // verificaInattivita() è un metodo private!
        Method metodoWatchdog = ControllerMonitoraggio.class.getDeclaredMethod("verificaInattivita");
        metodoWatchdog.setAccessible(true); 
        metodoWatchdog.invoke(controllerMonitor);


        assertFalse(lampada.isOnline(), "Il dispositivo doveva essere OFFLINE");
        
        boolean evento = cronologia.getCronologia().stream()
                                  .anyMatch(e -> e.getTipo().equals("DISCONNESSIONE") && e.getIdTarget().equals("LampadaSalotto"));
        assertTrue(evento, "L'evento non è stato salvato nella cronologia.");
    }
}