package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import domotica.services.MsgListener;
import domotica.services.RichiestaSH;
import domotica.services.ServizioRete;

import java.io.IOException;
import java.util.List;

public class SequenzaComandiTest {


    // TC-01: Costruttore (Happy Path)
    @Test
    public void testCostruttore_HappyPath() {
        SequenzaComandi seq = new SequenzaComandi("SEQ-01", "Macro Salotto");
        
        assertEquals("SEQ-01", seq.getId());
        assertEquals("Macro Salotto", seq.getDisplayName());
    }

    // TC-02: Costruttore (Parametri Invalidi)
    @Test
    public void testCostruttore_IdVuotoOBlank() {
        assertThrows(IllegalArgumentException.class, () -> new SequenzaComandi("", "Macro"));
        assertThrows(IllegalArgumentException.class, () -> new SequenzaComandi("   ", "Macro"));
    }

    @Test
    public void testCostruttore_ParametriNulli() {
        assertThrows(NullPointerException.class, () -> new SequenzaComandi(null, "Macro"));
        assertThrows(NullPointerException.class, () -> new SequenzaComandi("SEQ-01", null));
    }
    
    /**
     * Ci serve solo per contare quante volte la Sequenza lo chiama.
     */
    class ComandoFinto extends Comando {
        public int chiamate = 0;

        @Override
        public void esegui(Target t, ServizioRete rete, String source) {
            this.chiamate++;
        }

		@Override
		public void esegui(Target t, ServizioRete rete, String source, long sourceTimestamp) {
			this.chiamate++;
		}

    }
    
    class ReteFinta implements ServizioRete{
		@Override
		public void send(RichiestaSH req, String dest) throws IOException {}
		@Override
		public void listen(MsgListener listener) {}
    }
    
    class TargetFinto extends Target{
		protected TargetFinto(String id) {
			super(id);
		}

		@Override
		public List<Dispositivo> getDispositivi() {return null;}
		@Override
		public List<Dispositivo> getDispositiviCompatibili(ComandoSingolo c) {return null;}

    }

    // TC-03: Aggiunta Comandi
    @Test
    public void testAddComando_AggiuntaValida() {
        SequenzaComandi seq = new SequenzaComandi("SEQ-01", "Macro");
        ComandoFinto cmd1 = new ComandoFinto();
        
        seq.addComando(cmd1);

        assertDoesNotThrow(() -> seq.addComando(new ComandoFinto()));
    }

    @Test
    public void testAddComando_ComandoNullo() {
        SequenzaComandi seq = new SequenzaComandi("SEQ-01", "Macro");
        
        IllegalArgumentException eccezione = assertThrows(
            IllegalArgumentException.class, 
            () -> seq.addComando(null)
        );
        assertEquals("Impossibile aggiungere un comando nullo alla sequenza.", eccezione.getMessage());
    }


    // TC-04: Aggiunta Comandi: Anti-loop
    @Test
    public void testAddComando_AggiungiSeStessa(){
        SequenzaComandi seq = new SequenzaComandi("SEQ-01", "Macro");
        
        seq.addComando(seq);
        
        assertFalse(seq.getComandi().contains(seq));
    }

    
    // TC-05: Aggiunta Comandi: Altra sequenza
    @Test
    public void testAddComando_AggiungeAltraSequenza() {
        SequenzaComandi seqPadre = new SequenzaComandi("SEQ-01", "Macro Padre");
        SequenzaComandi seqFiglia = new SequenzaComandi("SEQ-02", "Macro Figlia");
        
        seqPadre.addComando(seqFiglia);
        
        assertTrue(seqPadre.getComandi().contains(seqFiglia));
    }

    
    

    // TC-06: Esecuzione
    
    @Test
    public void testEsegui_paramNull() throws Exception {
    	SequenzaComandi seq = new SequenzaComandi("SEQ-01", "Macro Test");
    	assertThrows(NullPointerException.class, () -> seq.esegui(null, new ReteFinta(), "user"));
    	assertThrows(NullPointerException.class, () -> seq.esegui(new TargetFinto("target"), null, "user"));
    }
    
    
    @Test
    public void testEsegui() throws Exception {
        SequenzaComandi seq = new SequenzaComandi("SEQ-01", "Macro Test");

        ComandoFinto cmd1 = new ComandoFinto();
        ComandoFinto cmd2 = new ComandoFinto();
        ComandoFinto cmd3 = new ComandoFinto();
        
        seq.addComando(cmd1);
        seq.addComando(cmd2);
        seq.addComando(cmd3);
        
        seq.esegui(new TargetFinto("target"), new ReteFinta(), "user");
        
        assertEquals(1, cmd1.chiamate, "Il comando 1 non è stato eseguito");
        assertEquals(1, cmd2.chiamate, "Il comando 2 non è stato eseguito");
        assertEquals(1, cmd3.chiamate, "Il comando 3 non è stato eseguito");
    }
}