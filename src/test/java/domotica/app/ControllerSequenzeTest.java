package domotica.app;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import domotica.domain.Comando;
import domotica.domain.Cronologia;
import domotica.domain.RegistroSequenze;
import domotica.domain.SequenzaComandi;
import domotica.domain.RegistroTargets;
import domotica.services.MsgListener;
import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

public class ControllerSequenzeTest {

    private RegistroSequenze registroSeq;
    private SpyControllerTargets ctSpy;
    private ControllerSequenze controller;


    class ReteDiTest implements ServizioRete {
        @Override public void send(RichiestaSH req, String dest) {}
        @Override public void listen(MsgListener listener) {}
    }

    class SpyControllerTargets extends ControllerTargets {
        public int chiamateEsegui = 0;
        public Comando comandoRicevuto = null;
        public String targetRicevuto = null;

        public SpyControllerTargets() {
            super(new RegistroTargets(), new ReteDiTest(), new Cronologia());
        }

        @Override
        public void eseguiComando(Comando c, String idTarget, String source){
            this.chiamateEsegui++;
            this.comandoRicevuto = c;
            this.targetRicevuto = idTarget;
        }
    }

    @BeforeEach
    public void setup() {
        registroSeq = new RegistroSequenze();
        ctSpy = new SpyControllerTargets();
        controller = new ControllerSequenze(registroSeq, ctSpy);
    }

    // TC01: testCostruttore
    @Test
    public void testCostruttore_ParametriNulli() {
        assertThrows(NullPointerException.class, () -> {
            new ControllerSequenze(null, ctSpy);
        });

        assertThrows(NullPointerException.class, () -> {
            new ControllerSequenze(registroSeq, null);
        });
    }

    // TC02: testGetSequenze
    @Test
    public void testGetSequenze() {
        registroSeq.addSequenza(new SequenzaComandi("SEQ-1", "Mattina"));
        registroSeq.addSequenza(new SequenzaComandi("SEQ-2", "Notte"));

        List<String> lista = controller.getSequenze();

        assertEquals(2, lista.size(), "Dovrebbero esserci 2 sequenze nel registro");
        assertTrue(lista.contains("SEQ-1"));
        assertTrue(lista.contains("SEQ-2"));
    }

    // TC03: testEseguiSequenza_ParametriNulli
    @Test
    public void testEseguiSequenza_ParametriNulli() {
        assertThrows(NullPointerException.class, () -> {
            controller.eseguiSequenza(null, "Target-1");
        });

        assertThrows(NullPointerException.class, () -> {
            controller.eseguiSequenza("SEQ-1", null);
        });
    }

    // TC04: testEseguiSequenza_SequenzaNonTrovata
    @Test
    public void testEseguiSequenza_SequenzaNonTrovata() {
        assertThrows(IllegalArgumentException.class, () -> {
            controller.eseguiSequenza("ID-FALSO", "Target-1");
        }, "Deve lanciare eccezione se la sequenza non esiste nel registro");

        assertEquals(0, ctSpy.chiamateEsegui);
    }

    // TC05: testEseguiSequenza_Successo
    @Test
    public void testEseguiSequenza_Successo() throws Exception {
        SequenzaComandi seqVera = new SequenzaComandi("SEQ-1", "Mattina");
        registroSeq.addSequenza(seqVera);

        controller.eseguiSequenza("SEQ-1", "Target-1");

        assertEquals(1, ctSpy.chiamateEsegui, "Il ControllerTargets doveva essere chiamato esattamente 1 volta");
        assertEquals("Target-1", ctSpy.targetRicevuto, "L'ID del target non è stato passato correttamente");
        assertEquals(seqVera, ctSpy.comandoRicevuto, "La sequenza passata al ControllerTargets non è quella giusta");
    }
}