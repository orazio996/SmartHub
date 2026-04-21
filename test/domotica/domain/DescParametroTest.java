package domotica.domain;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.List;

public class DescParametroTest {

    // TC-01: Test readOnly
    @Test
    public void testIsValoreValido_SensoreReadOnly() {
        DescParametro sensore = new DescParametro("temp", "float", "C", true, "0", "50", null);
        assertFalse(sensore.isValoreValido("25"), "Un sensore read-only non può accettare comandi");
    }

    // TC-02: Test valori accettabili
    @Test
    public void testIsValoreValido_ListaValori() {
        DescParametro switchLuce = new DescParametro("power", "string", "", false, "", "", List.of("ON", "OFF"));
        
        assertTrue(switchLuce.isValoreValido("ON"));
        assertTrue(switchLuce.isValoreValido("OFF"));
        assertFalse(switchLuce.isValoreValido("valore non valido"), "Deve scartare valori non in lista");
    }

    // TC-03: test range di valori accettabili o non
    @Test
    public void testIsValoreValido_RangeNumerico() {
        DescParametro luminosita = new DescParametro("luminosita", "int", "%", false, "0", "100", null);
        
        // In Range
        assertTrue(luminosita.isValoreValido("50"));
        assertTrue(luminosita.isValoreValido("0")); 
        assertTrue(luminosita.isValoreValido("100")); 
        
        // Out of Range
        assertFalse(luminosita.isValoreValido("-1"), "Sotto il minimo");
        assertFalse(luminosita.isValoreValido("101"), "Sopra il massimo");
    }

    // TC-04: Test formato numerico
    @Test
    public void testIsValoreValido_ValoreNonNumerico() {
        DescParametro temperatura = new DescParametro("temperatura", "float", "C", false, "10", "30", null);
        
        // lettere al posto di numeri
        assertFalse(temperatura.isValoreValido("venti"), "Deve catturare la NumberFormatException e restituire false");
    }
}