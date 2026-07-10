package domotica.domain;

import java.util.Objects;

public class TipoMetrica {
    
	private String nome;
    private String descrizione;

    public TipoMetrica(String nome, String descrizione) {
        
        this.nome = Objects.requireNonNull(nome, "Il nome del tipo metrica non può essere nullo.");
        this.descrizione = Objects.requireNonNull(descrizione, "La descrizione non può essere nulla.");
        
        if (nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del tipo metrica non può essere vuoto.");
        }
    }
    
    public String getNome() { return nome; }
    public String getDescrizione() { return descrizione; }
    @Override
	public String toString() {
		return "TipoMetrica [nome=" + nome + ", descrizione=" + descrizione + "]";
	}
}
