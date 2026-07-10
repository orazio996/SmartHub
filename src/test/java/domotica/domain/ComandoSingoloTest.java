package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import domotica.services.MsgListener;
import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

public class ComandoSingoloTest {
	
	class ServizioReteFinto implements ServizioRete {
        public int chiamateEffettuate = 0;
        public List<String> indirizziContattati = new ArrayList<>();

        @Override
        public void send(RichiestaSH req, String indirizzo) throws IOException {
            this.chiamateEffettuate++;
            this.indirizziContattati.add(indirizzo);
        }

		@Override
		public void listen(MsgListener listener) {}
    }

    class TargetFinto extends Target {
        private List<Dispositivo> dispositivi;

        public TargetFinto(List<Dispositivo> dispositivi) {
            super("idFinto"); 
            this.dispositivi = dispositivi;
        }

        @Override
        public List<Dispositivo> getDispositiviCompatibili(ComandoSingolo c) {
            return this.dispositivi;
        }

		@Override
		public List<Dispositivo> getDispositivi() {return null;}
    }

    class DispositivoFinto extends Dispositivo {
        private String indirizzo;

        public DispositivoFinto(String indirizzo) {
            super("idFinto", indirizzo, new DescDispositivo("marca", "tipo", "modello", Map.of("power", new DescParametro("power","" ,"",false,"","",List.of("ON","OFF")))));
            this.indirizzo = indirizzo;
        }

        public String getIndirizzo() {
            return this.indirizzo;
        }
		@Override
		public List<Dispositivo> getDispositivi() {return null;}
		@Override
		public List<Dispositivo> getDispositiviCompatibili(ComandoSingolo c) {return null;}
    }

	// TC-01: test costruttore happy path
    @Test
    public void testCostruttore_HappyPath() {
        ComandoSingolo comando = new ComandoSingolo("luminosita", "80");
        
        assertEquals("luminosita", comando.getParam());
        assertEquals("80", comando.getValore());
    }

    
    // TC-02: test costruttore comando invalido
    @Test
    public void testCostruttore_ComandoInvalido() {
        assertThrows(NullPointerException.class, () -> new ComandoSingolo(null, "80"));
        assertThrows(IllegalArgumentException.class, () -> new ComandoSingolo("", "80"));
        assertThrows(IllegalArgumentException.class, () -> new ComandoSingolo("   ", "80"));
        assertThrows(NullPointerException.class, () -> new ComandoSingolo("luminosita", null));
    }

    // TC-03: testCostruttoreJson_HappyPath
    @Test
    public void testCostruttoreJson_HappyPath() {
        String jsonValido = "{\"param\":\"temperatura\", \"valore\":\"22.5\"}";
        ComandoSingolo comando = new ComandoSingolo(jsonValido);
        
        assertEquals("temperatura", comando.getParam(), "Il parametro estratto dal JSON non è corretto");
        assertEquals("22.5", comando.getValore(), "Il valore estratto dal JSON non è corretto");
    }

    // TC-04: testCostruttoreJson_StringaVuota
    @Test
    public void testCostruttoreJson_StringaVuota() {
        assertThrows(IllegalArgumentException.class, () -> new ComandoSingolo((String) null), "Deve lanciare eccezione per JSON null");
        assertThrows(IllegalArgumentException.class, () -> new ComandoSingolo(""), "Deve lanciare eccezione per JSON vuoto");
        assertThrows(IllegalArgumentException.class, () -> new ComandoSingolo("   "), "Deve lanciare eccezione per JSON blank");
    }

    // TC-05: testCostruttoreJson_JsonMalformato
    @Test
    public void testCostruttoreJson_JsonMalformato() {

        String jsonRotto = "json malformato";
        assertThrows(IllegalArgumentException.class, () -> new ComandoSingolo(jsonRotto), "Deve lanciare eccezione se la stringa non è un JSON valido");

        // chiavi mancanti
        String jsonSenzaChiavi = "{\"paramSbagliato\":\"luminosita\"}";
        assertThrows(IllegalArgumentException.class, () -> new ComandoSingolo(jsonSenzaChiavi), "Deve lanciare eccezione se il JSON non ha le chiavi attese");
    }

    // TC-06: testEsegui
    @Test
    public void testEsegui() throws Exception {
        ComandoSingolo comando = new ComandoSingolo("power", "ON");
        ServizioReteFinto rete = new ServizioReteFinto();
        
        List<Dispositivo> lista = Arrays.asList(
            new DispositivoFinto("luceSalotto"),
            new DispositivoFinto("luceCucina")
        );
        TargetFinto target = new TargetFinto(lista);

        comando.esegui(target, rete, "user");

        assertEquals(2, rete.chiamateEffettuate, "Dovevano partire 2 richieste");
        assertTrue(rete.indirizziContattati.contains("luceSalotto"));
        assertTrue(rete.indirizziContattati.contains("luceCucina"));
    }
}