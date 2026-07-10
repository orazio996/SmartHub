package domotica.domain;

import java.util.Objects;

public class Routine {
    
    private String nome;
    private boolean isAbilitata;
    private Target target;
    private ComandoSingolo comando;
    private Trigger trigger;

    public Routine(String nome, Target target, ComandoSingolo comando, Trigger trigger) {

    	Objects.requireNonNull(nome);

        if (nome.isBlank()) {
            throw new IllegalArgumentException("Il nome della routine non può essere vuoto");
        }
        
        this.nome = nome;
        this.target = Objects.requireNonNull(target);
        this.comando = Objects.requireNonNull(comando);
        this.trigger = Objects.requireNonNull(trigger);

        this.isAbilitata = true; 
    }

    public String getNome() {
        return nome;
    }

    public Target getTarget() {
        return target;
    }

    public ComandoSingolo getComando() {
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