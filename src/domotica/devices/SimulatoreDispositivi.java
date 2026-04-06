package domotica.devices;

public class SimulatoreDispositivi {
    public static void main(String[] args) {
        System.out.println("🔌 INIZIALIZZAZIONE DISPOSITIVI...\n");

        Lampadina lampadaScrivania = new Lampadina(8080, "00:1A:2B:3C:4D:5E", "Lampadina", "Philips", "Hue White 9W");
        Lampadina lamapdaNotte  = new Lampadina(8081, "00:5E:2B:4D:4D:2B", "Lampadina", "Tapoo", "5E355 12W");
        Termostato termostato = new Termostato(8090, "00:1E:4B:6D:8D:2B", "Termostato", "Samsung", "SuperWarm 2000X");

        new Thread(lampadaScrivania).start();
        new Thread(lamapdaNotte).start();
        new Thread(termostato).start();
    }
}