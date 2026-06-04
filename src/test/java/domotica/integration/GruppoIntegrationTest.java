package domotica.integration;

import domotica.domain.*;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GruppoIntegrationTest {

    @Test
    public void testSmistamento_GruppoFiltraDispositivi() {
        DescParametro paramLuce = new DescParametro("stato", "string", "", false, "", "", List.of("ON", "OFF"));
        Map<String, DescParametro> mappaLuce = new HashMap<>(); mappaLuce.put("stato", paramLuce);
        DescDispositivo descLampada = new DescDispositivo("Ph", "Luce", "Mod", mappaLuce);
        
        Dispositivo luce1 = new Dispositivo("Luce_1", "ip1", descLampada);
        Dispositivo luce2 = new Dispositivo("Luce_2", "ip2", descLampada);

        DescParametro paramTv = new DescParametro("volume", "int", "", false, "0", "100", null);
        Map<String, DescParametro> mappaTv = new HashMap<>(); mappaTv.put("volume", paramTv);
        Dispositivo tv = new Dispositivo("TV_1", "ip3", new DescDispositivo("Lg", "TV", "Mod", mappaTv));

        Gruppo salotto = new Gruppo("Salotto", false, true);
        salotto.addTarget(luce1);
        salotto.addTarget(luce2);
        salotto.addTarget(tv);

        Comando comandoLuce = new Comando("stato", "ON");
        List<Dispositivo> compatibili = salotto.getDispositiviCompatibili(comandoLuce);

        assertEquals(2, compatibili.size(), "Deve trovare le due luci e scartare la TV");
    }
}