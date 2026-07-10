package domotica.integration;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import domotica.app.*;
import domotica.domain.ArchivioReports;
import domotica.domain.ComandoSingolo;
import domotica.domain.Cronologia;
import domotica.domain.DescDispositivo;
import domotica.domain.DescParametro;
import domotica.domain.Dispositivo;
import domotica.domain.Evento;
import domotica.domain.FactoryMetriche;
import domotica.domain.MotoreRoutine;
import domotica.domain.RegistroSequenze;
import domotica.domain.RegistroTargets;
import domotica.domain.Report;
import domotica.domain.SequenzaComandi;
import domotica.domain.TransizioneStato;
import domotica.services.ServizioReteTCP;

public class SystemIntegrationTest {
	
	/**
     * crea un evento di tipo tipo per il target target che cambia il parametro 'power' da prima a dopo
     */
    public Evento newEvento(String tipo, String prima, String dopo, LocalDateTime timestamp, String target) {
    	long timestampMs = timestamp.atZone(ZoneId.systemDefault())
    		    .toInstant()
    		    .toEpochMilli();
    	return new Evento("source", System.currentTimeMillis(), tipo, target, List.of(new TransizioneStato("power", prima, dopo, target)), timestampMs);
    }


    private RegistroTargets registro;
    private ArchivioReports archivio;
    private Cronologia cronologia;
    private RegistroSequenze registroSeq;

    private ControllerTargets controllerTargets;
    private ControllerMonitoraggio controllerMonitor;
    private ControllerRoutines controllerRoutines;
    private ControllerMetriche controllerMetriche;
    private ControllerSequenze controllerSequenze;
   
    private ServizioReteTCP servizioRete;
    private MotoreRoutine motoreRoutine;
    private FactoryMetriche factory;

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
        archivio = new ArchivioReports();
        cronologia = new Cronologia();
        factory = new FactoryMetriche();
        registroSeq = new RegistroSequenze();

        servizioRete = new ServizioReteTCP(hubPort);
        
        controllerTargets = new ControllerTargets(registro, servizioRete, cronologia);
        motoreRoutine = new MotoreRoutine(controllerTargets);
        controllerMonitor = new ControllerMonitoraggio(registro, servizioRete);
        controllerMetriche = new ControllerMetriche(registro, archivio, cronologia, factory, List.of("Consumo", "Uptime", "Media", "min", "Max", "n_Disconn"));
        controllerRoutines = new ControllerRoutines(motoreRoutine, registro);
        controllerSequenze = new ControllerSequenze(registroSeq, controllerTargets);
        
        controllerMonitor.addMonitorListener(cronologia);
        controllerMonitor.addMonitorListener(motoreRoutine);


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
        
        lampada.setStatoConn(true);
        termostato.setStatoConn(true);
        sensore.setStatoConn(true);

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
                 BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                 PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                msg.complete(in.readLine());
                out.println("{\"status\": \"OK\"}");
            } catch (Exception e) {
                msg.completeExceptionally(e);
            }
        });
        hardwareFisico.start();
        Thread.sleep(200);

        // eseguo il comando
        controllerTargets.eseguiComando("power", "ON", "LampadaSalotto", "user");

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
        String msgSensore = "{\"idTarget\":\"SensoreTemp\", \"tipo\":\"sensorCmd\", \"parametro\":\"temperatura\", \"valore\":\"16.0\"}";        try (Socket socketVersoHub = new Socket("127.0.0.1", hubPort);
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

        
        String msg = "{\"idTarget\":\"LampadaSalotto\", \"tipo\":\"userCmd\", \"parametro\":\"power\", \"valore\":\"ON\"}";
        // dispositivo di test che manda msg
        try (Socket socketVersoHub = new Socket("127.0.0.1", hubPort);
             PrintWriter out = new PrintWriter(socketVersoHub.getOutputStream(), true)) {
            out.println(msg);
        }

        Thread.sleep(500);

        assertEquals("ON", lampada.getStato().get("power"), "Lo stato del dispositivo non è stato aggiornato");
        assertEquals(eventiIniziali + 1, cronologia.getCronologia().size(), "La cronologia non e stata aggiornata");
        assertEquals("userCmd", cronologia.getCronologia().get(eventiIniziali).getTipo(), "Tipo sbagliato");
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
    
    
 // UC1.3: Consulta Metriche
    @Test
    public void testUC1_3_ConsultaMetriche() {
        
        lampada.setPotenza(2.0);

        Evento eventoOn = newEvento("userCmd", "OFF", "ON", LocalDateTime.now().minusDays(2), "LampadaSalotto");
        Evento eventoOff = newEvento("userCmd", "ON", "OFF", LocalDateTime.now().minusDays(1), "LampadaSalotto");

        cronologia.addEvento(eventoOn);
        cronologia.addEvento(eventoOff);

        LocalDate dataInizio = LocalDate.now().minusDays(5);
        LocalDate dataFine = LocalDate.now();

        Report reportGenerato = controllerMetriche.generaReport(
            "LampadaSalotto", 
            "param",
            List.of("Consumo"), 
            dataInizio, 
            dataFine
        );

        controllerMetriche.salvaReport(reportGenerato, "Report Lampada Salotto");
        
        assertNotNull(reportGenerato, "Il Controller non ha restituito il Report");
        assertEquals("Report Lampada Salotto", reportGenerato.getNome());


        assertEquals(1, archivio.getReports().size(), "Il report non è stato salvato nell'Archivio");
        assertEquals(reportGenerato, archivio.getReports().get(0));


        assertEquals(1, reportGenerato.getMetriche().size(), "Il report non contiene la metrica");
        assertEquals("kWh", reportGenerato.getMetriche().get(0).getUnitaMisura());
        
        // 24h * 2.0kW = 48.0 kWh)
        double consumoCalcolato = reportGenerato.getMetriche().get(0).getValore();
        assertEquals(48.0, consumoCalcolato, 0.1, "Calcolo consumo errato");
    }
    
    
@Test
    public void testUC08_EseguiSequenza() throws Exception {
        Dispositivo target = (Dispositivo) registro.getTarget("Termostato");
        int porta = Integer.parseInt(target.getIndirizzo().split(":")[1]);

        new Thread(() -> {
            try (ServerSocket server = new ServerSocket(porta)) {
                for (int i = 0; i < 2; i++) {
                    try (Socket client = server.accept();
                         BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                         PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {
                        
                        String req = in.readLine();
                        out.println("{\"status\": \"OK\"}");
                        
                        if (req != null) {
                            com.google.gson.JsonObject j = com.google.gson.JsonParser.parseString(req).getAsJsonObject();
                            String msgPerHub = String.format(
                                "{\"sourceTarget\":\"%s\", \"idTarget\":\"Termostato\", \"tipo\":\"%s\", \"parametro\":\"%s\", \"valore\":\"%s\", \"source\":\"%s\", \"sourceTimestamp\":\"%s\"}", 
                                j.get("sourceTarget").getAsString(), j.get("tipo").getAsString(), j.get("param").getAsString(), j.get("val").getAsString(), j.get("source").getAsString(), j.get("sourceTimestamp").getAsString()
                            );
                            
                            try (Socket sHub = new Socket("127.0.0.1", hubPort);
                                 PrintWriter outHub = new PrintWriter(sHub.getOutputStream(), true)) {
                                outHub.println(msgPerHub);
                            }
                        }
                    }
                }
            } catch (Exception e) {}
        }).start();

        Thread.sleep(500); 

        SequenzaComandi seq = new SequenzaComandi("idSeq", "idSeq");
        seq.addComando(new ComandoSingolo("power","ON"));
        seq.addComando(new ComandoSingolo("wait","3000")); 
        seq.addComando(new ComandoSingolo("power","OFF"));
        
        SequenzaComandi seq1 = new SequenzaComandi("idSeq1", "idSeq1");
        seq1.addComando(seq);
        registroSeq.addSequenza(seq1);
        
        cronologia.getCronologia().clear();
        controllerSequenze.eseguiSequenza("idSeq1", "Termostato");
        
        Thread.sleep(500);
        
        assertEquals(2, cronologia.getCronologia().size());
        long ts1 = cronologia.getCronologia().getFirst().getTimestamp();
        long ts2 = cronologia.getCronologia().getLast().getTimestamp();
        assertTrue(Math.abs(ts1 - ts2) >= 2900);
    }
    
}