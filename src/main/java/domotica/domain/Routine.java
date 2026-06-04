package domotica.domain;

public class Routine {
    
    private String nome;
    private boolean isAbilitata;
    private Target target;
    private Comando comando;
    private Trigger trigger;

    public Routine(String nome, Target target, Comando comando, Trigger trigger) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Il nome della routine non può essere vuoto");
        }
        if (target == null || comando == null || trigger == null) {
            throw new IllegalArgumentException("Target, Comando e Trigger non possono essere nulli");
        }
        
        this.nome = nome;
        this.target = target;
        this.comando = comando;
        this.trigger = trigger;

        this.isAbilitata = true; 
    }

    public String getNome() {
        return nome;
    }

    public Target getTarget() {
        return target;
    }

    public Comando getComando() {
        return comando;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public boolean isAbilitata() {
        return isAbilitata;
    }

    public void setAbilitata(boolean isAbilitata) {
        this.isAbilitata = isAbilitata;
    }

    @Override
    public String toString() {
        return "Routine{" +
                "nome='" + nome + '\'' +
                ", isAbilitata=" + isAbilitata +
                ", target=" + target.getId() +
                ", comando=" + comando.getParam() + "=" + comando.getValore() +
                '}';
    }
}