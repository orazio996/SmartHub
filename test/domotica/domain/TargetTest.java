package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class TargetTest {

    // TC-01: Happy Path
    @Test
    public void testCostruttore_IdValido() {
        Target fintoTarget = newTarget("GRUPPO_1");
        assertEquals("GRUPPO_1", fintoTarget.getId(), "l'ID deve corrispondere");
    }

    // TC-02: id nullo
    @Test
    public void testCostruttore_IdNullo() {
        assertThrows(IllegalArgumentException.class, () -> {
        	newTarget(null);
        }, "Deve lanciare eccezione se l'id è nullo");
    }

    // TC-03: id vuoto
    @Test
    public void testCostruttore_IdVuoto() {
        assertThrows(IllegalArgumentException.class, () -> {
        	newTarget("   ");
        }, "Deve lanciare un eccezione se l'ID è vuoto ");
        
        assertThrows(IllegalArgumentException.class, () -> {
        	newTarget("");
        }, "Deve lanciare un eccezione se l'ID è vuoto ");
    }

    /**
     * Metodo di supporto: Crea una classe anonima "usa e getta" 
     * solo per testare la logica base di Target.
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