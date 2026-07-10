package domotica.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import domotica.domain.DescParametro;
import domotica.domain.Dispositivo;
import domotica.domain.Gruppo;
import domotica.domain.Target;
import domotica.ui.dto.DispositivoDTO;
import domotica.ui.dto.GruppoDTO;
import domotica.ui.dto.DescParametroDTO;
import domotica.ui.dto.TargetDTO;

public class TargetMapper {

    public static TargetDTO creaDTO(Target target) {
        if (target instanceof Dispositivo) {
            Dispositivo d = (Dispositivo) target;
            
            // mappatura parametri
            Map<String, DescParametroDTO> parametriDTO = new HashMap<>();
            Map<String, DescParametro> mappaParametriOriginale = d.getDescrizione().getDescParametri();
            
            if (mappaParametriOriginale != null) {
                for (Map.Entry<String, DescParametro> entry : mappaParametriOriginale.entrySet()) {
                    DescParametro p = entry.getValue();
                    parametriDTO.put(entry.getKey(), new DescParametroDTO(
                        p.getNome(),
                        p.getUnitaMisura(),
                        p.getMin(),
                        p.getMax(),
                        p.getValoriAccettati(),
                        p.isReadOnly()
                    ));
                }
            }

            // mappatura stato
            Map<String, String> statoCopia = new HashMap<>();
            if (d.getStato() != null) {
                statoCopia.putAll(d.getStato());
            }

            return new DispositivoDTO(
                d.getId(),
                "DISPOSITIVO",
                d.getDescrizione().getMarca(),
                d.getDescrizione().getModello(),
                statoCopia,
                parametriDTO
            );
            
        } else if (target instanceof Gruppo) {
            Gruppo g = (Gruppo) target;
            
            // mappatura figli
            List<TargetDTO> figliDTO = new ArrayList<>();
            if (g.getFigli() != null) {
                for (Target figlio : g.getFigli()) {
                    figliDTO.add(creaDTO(figlio)); 
                }
            }
            
            return new GruppoDTO(
                g.getId(),
                "GRUPPO",
                figliDTO
            );
        }
        
        throw new IllegalArgumentException("Tipo di Target sconosciuto: Impossibile creare il DTO.");
    }
}