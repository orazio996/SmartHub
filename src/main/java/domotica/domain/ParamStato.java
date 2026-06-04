package domotica.domain;

/**
 * Singola voce dello stato attuale di un dispositivo.
 * (Es. nome="temperatura", valore="22.5")
 */
public class ParamStato {
    
    private String nome;
    private String valore;

    public ParamStato(String nome, String valore) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome del parametro non può essere vuoto");
        }
        this.nome = nome;
        this.valore = valore;
    }


    public String getNome() {
        return nome;
    }

    public String getValore() {
        return valore;
    }

    public void setValore(String valore) {
        this.valore = valore;
    }

    @Override
    public String toString() {
        return "[" + nome + ": " + valore + "]";
    }
}