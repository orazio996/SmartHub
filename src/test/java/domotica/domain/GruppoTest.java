package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GruppoTest {

    private Gruppo gruppo;

    @BeforeEach
    public void setup() {
    	gruppo = new Gruppo("CUCINA", false, true);
    }


    // TC-01: add e remove Happy Path
    @Test
    public void testAddAndRemove_TargetValido() {
        
        Gruppo sottoGruppo = new Gruppo("sottoGruppo", false, false);
        
        gruppo.addTarget(sottoGruppo);
        assertEquals(1, gruppo.getFigli().size(), "Deve aggiungere il sottoGruppo");
        assertTrue(gruppo.getFigli().contains(sottoGruppo));
        
        gruppo.removeTarget(sottoGruppo);
        assertTrue(gruppo.getFigli().isEmpty(), "Deve rimuovere il sottoGruppo");
    }

    // TC-02: add e remove target nullo
    @Test
    public void testAddAndRemove_TargetNullo() {
        
        assertThrows(IllegalArgumentException.class, () -> {
            gruppo.addTarget(null);
        }, "Deve lanciare eccezione se si tenta di aggiungere null");

        assertThrows(IllegalArgumentException.class, () -> {
        	gruppo.removeTarget(null);
        }, "Deve lanciare eccezione se si tenta di rimuovere null");

        assertTrue(gruppo.getFigli().isEmpty(), "Non deve aver aggiunto ne rimosso");
    }

    // TC-03: Controllo Anti-Loop
    @Test
    public void testAddTarget_StessoGruppo() {
    	gruppo.addTarget(gruppo);
        
        assertTrue(gruppo.getFigli().isEmpty(), "Deve bloccare l'inserimento del gruppo a se stesso");
    }

    // TC-04: test della ricorsione
    //gruppo_____sottoGruppo1_____gruppoFinto1_____(1 dispositivi)
    //        |                 |__gruppoFinto3____(3 dispositivi)
    //		  |
    //        |_____sottoGruppo2_____gruppoFinto2_____(2 dispositivi)        
    //        				        |_____gruppoFinto3_____(3 dispositivi) 

    @Test
    public void testGetDispositivi_Ricorsione() {

        Target gruppoFinto1 = newTarget("gruppo1", 1);
        Target gruppoFinto2 = newTarget("gruppo2", 2);
        Target gruppoFinto3 = newTarget("gruppo3", 3); 

        Gruppo sottoGruppo1 = new Gruppo("sottoGruppo1", false, true);
        Gruppo sottoGruppo2 = new Gruppo("sottoGruppo2", false, true);
        sottoGruppo1.addTarget(gruppoFinto1);
        sottoGruppo1.addTarget(gruppoFinto3);
        sottoGruppo2.addTarget(gruppoFinto2);
        sottoGruppo2.addTarget(gruppoFinto3);

        gruppo.addTarget(sottoGruppo1);
        gruppo.addTarget(sottoGruppo2);
        
        List<Dispositivo> dispositivi = gruppo.getDispositivi();
        
        // il gruppo deve contare solo 6 dispositivi senza ripetuti
        assertEquals(6, dispositivi.size(), "Devono essere restituiti tutti e solo i dispositivi non ripetuti");
    }
    
    @Test
    public void testGetDispositivi_LoopInfinito() {
        Gruppo a = new Gruppo("A", false, true);
        Gruppo b = new Gruppo("B", false, true);
        
        a.addTarget(b);
        b.addTarget(a);
        
        // deve gestire la dipendenza incrociata senza andare in loop infinito
        assertDoesNotThrow(() -> {
            List<Dispositivo> risultato = a.getDispositivi();
            assertTrue(risultato.isEmpty(), "Con solo gruppi in loop, la lista deve essere vuota");
        }, "Il sistema deve gestire i cicli infiniti senza andare in StackOverflow");
    }
    
    @Test
    public void testGetDispositiviCompatibili_FiltraCorrettamente() {
        Comando comando = new Comando("luce", "ON");

        Target targetCompatibile = new Target("targetCompatibile") {
            @Override public List<Dispositivo> getDispositivi() { return new ArrayList<>(); }
            @Override public List<Dispositivo> getDispositiviCompatibili(Comando c) {
                return List.of(new Dispositivo("devCompatibile", "ip", newDescParam()));
            }
        };

        Target targetIncompatibile = new Target("targetIncompatibile") {
            @Override public List<Dispositivo> getDispositivi() { return new ArrayList<>(); }
            @Override public List<Dispositivo> getDispositiviCompatibili(Comando c) {
                return new ArrayList<>();
            }
        };

        Gruppo gruppo = new Gruppo("CUCINA", false, true);
        gruppo.addTarget(targetCompatibile);
        gruppo.addTarget(targetIncompatibile);

        List<Dispositivo> compatibili = gruppo.getDispositiviCompatibili(comando);

        assertEquals(1, compatibili.size(), "Deve ignorare i target incompatibili");
        assertEquals("devCompatibile", compatibili.get(0).getId());
    }

    /**
     * Metodo di supporto: Crea un Target finto.
     * Il parametro 'numeroDispositiviFinti' ci permette di decidere quanti 
     * elementi restituirà alla chiamata getDispositivi().
     */
    private Target newTarget(String id, int numeroDispositivi) {

        List<Dispositivo> dispositivi = new ArrayList<>();
        for (int i = 0; i < numeroDispositivi; i++) {
            dispositivi.add(new Dispositivo(id + "_dev_" + i, "ip_finto", newDescParam()));
        }

        return new Target(id) {
            @Override
            public List<Dispositivo> getDispositivi() {
                return dispositivi; 
            }

            @Override
            public List<Dispositivo> getDispositiviCompatibili(Comando c) {
                return new ArrayList<>();
            }
        };
    }
    
    
    /**
     * Metodo di supporto: Crea una DescParametro finta.
     */
    private DescDispositivo newDescParam() {
        Map<String, DescParametro> mappaParametri = new HashMap<>();
        
        // Il costruttore esige almeno un parametro, quindi ne creiamo uno finto!
        // N.B: adatta "new DescParametro(...)" con i parametri richiesti dal tuo vero costruttore
        mappaParametri.put("stato_finto", new DescParametro("power", "string", "", false, "", "", List.of("ON","OFF"))); 
        
        return new DescDispositivo("FakeMarca", "FakeTipo", "FakeMod", mappaParametri);
    }
    
    
}