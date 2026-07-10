package domotica.ui.dto;

import java.util.Map;

public record DispositivoDTO(
    String id,
    String tipo,
    String marca,
    String modello,
    Map<String, String> statoAttuale, 
    Map<String, DescParametroDTO> parametri 
) implements TargetDTO {}