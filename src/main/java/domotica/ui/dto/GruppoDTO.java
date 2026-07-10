package domotica.ui.dto;

import java.util.List;

public record GruppoDTO(
    String id,
    String tipo,
    List<TargetDTO> figli 
) implements TargetDTO {}