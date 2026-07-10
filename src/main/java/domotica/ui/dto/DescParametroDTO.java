package domotica.ui.dto;

import java.util.List;

public record DescParametroDTO(
    String nome,
    String unitaMisura,
    String min,
    String max,
    List<String> valoriAccettati,
    boolean readOnly
) {}