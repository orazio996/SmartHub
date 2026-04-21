package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RegistroTargetsTest {

    private RegistroTargets registro;

    @BeforeEach
    public void setup() {
        registro = new RegistroTargets();
    }

    // TEST 1: Happy Path
    @Test
    public void testAddTarget_TargetValido() {
        Target t1 = newTarget("idTarget");
        
        registro.addTarget(t1);
        
        assertEquals(1, registro.getAllTargets().size(), "Il registro deve contenere 1 elemento");
        assertEquals(t1, registro.getTarget("idTarget"), "Deve restituire esattamente il target inserito");
    }

    // TEST 2: Target = null
    @Test
    public void testAddTarget_TargetNullo() {
        assertThrows(IllegalArgumentException.class, () -> {
            registro.addTarget(null);
        }, "Deve lanciare eccezione se si passa null");
    }

    // TEST 3: Target già esistente
    @Test
    public void testAddTarget_IdEsistente() {
        Target tVecchio = newTarget("idTarget");
        Target tNuovo = newTarget("idTarget");
        
        registro.addTarget(tVecchio);
        
        assertThrows(IllegalStateException.class, () -> {
            registro.addTarget(tNuovo);
        }, "Esiste già un target");

        assertEquals(1, registro.getAllTargets().size(), "La dimensione deve restare 1");
        assertEquals(tVecchio, registro.getTarget("idTarget"), "Il target recuperato deve essere quello vecchio");
    }

    // TEST 4: Rimozione happy path
    @Test
    public void testRemoveTarget_TargetEsistente() {
        Target t1 = newTarget("idTarget");
        registro.addTarget(t1);
        
        registro.removeTarget("idTarget");
        
        assertNull(registro.getTarget("idTarget"), "Il target non deve più esistere");
        assertEquals(0, registro.getAllTargets().size());
    }

    // TEST 5: Rimozione di un ID inesistente
    @Test
    public void testRemoveTarget_IdInesistente() {
        Target t1 = newTarget("idTarget");
        registro.addTarget(t1);
        
        assertDoesNotThrow(() -> {
            registro.removeTarget("idInesistente");
        }, "Rimuovere un ID inesistente non deve generare eccezioni");
        
        assertEquals(1, registro.getAllTargets().size(), "Il target iniziale deve essere ancora lì");
    }

    // TEST 6: GetTarget: id nullo o inesistente
    @Test
    public void testGetTarget_IdNulloOInesistente() {
        assertNull(registro.getTarget(null), "Se passo null deve restituire null");
        assertNull(registro.getTarget("idInesistente"), "Se l'ID non esiste deve restituire null");
    }

    // TEST 7: Factory Reset 
    @Test
    public void testFactoryReset_SvuotaTuttoIlRegistro() {
        registro.addTarget(newTarget("idTarget1"));
        registro.addTarget(newTarget("idTarget2"));
        
        registro.factoryReset();
        
        assertEquals(0, registro.getAllTargets().size(), "Il registro deve essere vuoto dopo il reset");
    }

    /**
     * Metodo di supporto: crea un Target al volo tramite Classe Anonima.
     */
    private Target newTarget(String id) {
        return new Target(id) {
            @Override
            public List<Dispositivo> getDispositivi() {
                return null;
            }

            @Override
            public List<Dispositivo> getDispositiviCompatibili(Comando c) {
                return null;
            }
        };
    }
}