package domotica.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CalcolatoreConsumoTest {

    private TipoMetrica tipoConsumo;
    private CalcolatoreConsumo calcolatore;
    private FiltroTemporale filtro;

    
    class DispositivoFinto extends Dispositivo {
        public DispositivoFinto(String id, double potenza) {
            super(id, "indirizzo", new DescDispositivo("marca", "tipo", "modello", Map.of("param", new DescParametro("param", "tipo", "uMisura", true, null, null, List.of("valore1")))));
            this.setPotenza(potenza); 
        }

        @Override
        public List<Dispositivo> getDispositivi() {
            List<Dispositivo> lista = new ArrayList<>();
            lista.add(this);
            return lista;
        }
    }
    
    /**
     * crea un evento di tipo tipo che cambia il parametro 'power' da prima a dopo
     */
    public Evento newEvento(String tipo, String prima, String dopo, LocalDateTime timestamp) {
    	long timestampMs = timestamp.atZone(ZoneId.systemDefault())
    		    .toInstant()
    		    .toEpochMilli();
    	return new Evento("source", System.currentTimeMillis() ,tipo, "target1", List.of(new TransizioneStato("power", prima, dopo, "target1")), timestampMs);
    }
    
    

    @BeforeEach
    public void setup() {
        tipoConsumo = new TipoMetrica("Consumo", "Calcolo dei Consumi");
        calcolatore = new CalcolatoreConsumo(tipoConsumo);
        filtro = new FiltroTemporale(LocalDate.now().minusWeeks(1), LocalDate.now());
    }

    
    // TC01: testCalcoloSenzaEventi
    @Test
    public void testCalcoloSenzaEventi() {
    	DispositivoFinto dispositivo = new DispositivoFinto("target1", 2.0);
        double risultato = calcolatore.calcola(dispositivo, "param", new ArrayList<>(), filtro);
        assertEquals(0.0, risultato);
    }

    
    // TC02: testCalcoloConEventi
    @Test
    public void testCalcoloConEventi() {
    	DispositivoFinto dispositivo = new DispositivoFinto("target1", 2.0); 

    	Evento eventoOn = newEvento("userCmd", "OFF", "ON", LocalDateTime.now().minusDays(6));
        Evento eventoDisconn = newEvento("DISCONNESSIONE", "ON", "ON", LocalDateTime.now().minusDays(5));
        Evento eventoConn = newEvento("CONNESSIONE", "ON", "ON", LocalDateTime.now().minusDays(4));
        Evento eventoOff = newEvento("userCmd", "ON", "OFF", LocalDateTime.now().minusDays(2));
        Evento eventoOn2 = newEvento("userCmd", "OFF", "ON", LocalDateTime.now().minusDays(1));

        List<Evento> eventi = new ArrayList<>();
        eventi.add(eventoOn);
        eventi.add(eventoDisconn);
        eventi.add(eventoConn);
        eventi.add(eventoOff);
        eventi.add(eventoOn2);

        long dataOraFineMs = filtro.getDataFine().atTime(LocalTime.now()).atZone(ZoneId.systemDefault())
				.toInstant()
				.toEpochMilli();
        double ore = (
	    		eventoDisconn.getTimestamp() - eventoOn.getTimestamp() +
	    		eventoOff.getTimestamp() - eventoConn.getTimestamp() +
	    		dataOraFineMs - eventoOn2.getTimestamp()
    		)/3600000.0;
        double consumoAtteso = ore*dispositivo.getPotenza();
        double risultato = calcolatore.calcola(dispositivo, "param", eventi, filtro);

        assertEquals(consumoAtteso, risultato, 0.1);
    }
}