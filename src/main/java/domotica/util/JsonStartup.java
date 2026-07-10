package domotica.util;

import domotica.app.*;
import domotica.domain.*;
import domotica.services.ServizioReteTCP;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

public class JsonStartup implements ServizioStartup {

    private final String percorsoFileConfig;
    private final int portaMonitoraggio;

    public JsonStartup(String percorsoFileConfig, int portaMonitoraggio) {
        this.percorsoFileConfig = percorsoFileConfig;
        this.portaMonitoraggio = portaMonitoraggio;
    }

    @Override
    public Context avviaSistema() throws Exception {
        
        RegistroTargets registro = new RegistroTargets();
        Cronologia cronologia = new Cronologia();
        ArchivioReports archivio = new ArchivioReports();
        FactoryMetriche factory = new FactoryMetriche();
        ServizioReteTCP servizioRete = new ServizioReteTCP(portaMonitoraggio);
        ControllerTargets controllerTargets = new ControllerTargets(registro, servizioRete, cronologia);
        MotoreRoutine motoreRoutine = new MotoreRoutine(controllerTargets);
        ControllerMonitoraggio controllerMonitor = new ControllerMonitoraggio(registro, servizioRete);
        ControllerRoutines controllerRoutines = new ControllerRoutines(motoreRoutine, registro);
        List<String> tipiMetrica = List.of("Consumo", "Uptime"); 
        ControllerMetriche controllerMetriche = new ControllerMetriche(registro, archivio, cronologia, factory, tipiMetrica);
        RegistroSequenze registroSeq = new RegistroSequenze(); 
        ControllerSequenze controllerSeq = new ControllerSequenze(registroSeq, controllerTargets);
        caricaDati(registro, cronologia, archivio, motoreRoutine, registroSeq, percorsoFileConfig);
        
        controllerMonitor.addMonitorListener(cronologia);
        controllerMonitor.addMonitorListener(motoreRoutine);
        controllerMonitor.start();

        return new Context(
            controllerTargets, 
            controllerRoutines, 
            controllerMetriche,
            controllerSeq,
            controllerMonitor,
            registro, 
            registroSeq,
            motoreRoutine,
            cronologia
        );
    }

    private void caricaDati(RegistroTargets registro, Cronologia c, ArchivioReports ar, MotoreRoutine mr, RegistroSequenze registroSeq, String cartellaConfig) {
        Gson gson = new Gson();

        // RegistroTargets
        try (InputStream is = getClass().getResourceAsStream(cartellaConfig + "/targets.json");
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            FileTargetsDTO datiTargets = gson.fromJson(reader, FileTargetsDTO.class);
            if (datiTargets != null) {
                
            	// dispositivi
                if (datiTargets.dispositivi != null) {
                    for (DispositivoDTO dev : datiTargets.dispositivi) {
                        Map<String, DescParametro> mappaParametri = new HashMap<>();
                        for (ParametroDTO param : dev.descrizione.parametri) {
                            mappaParametri.put(param.nome, new DescParametro(param.nome, param.tipo, param.unitaMisura, param.readOnly, param.min, param.max, param.valoriAmmessi));
                        }
                        DescDispositivo descD = new DescDispositivo(dev.descrizione.marca, dev.descrizione.tipo, dev.descrizione.modello, mappaParametri);
                        registro.addTarget(new Dispositivo(dev.id, dev.displayName, dev.indirizzoRete, descD, dev.potenza));
                    }
                }
                
                // gruppi
                if (datiTargets.gruppi != null) {
                    for (GruppoDTO gruppoDTO : datiTargets.gruppi) {
                        Gruppo gruppo = new Gruppo(gruppoDTO.id, gruppoDTO.displayName, gruppoDTO.predefinito, gruppoDTO.stanza);
                        for (String idMembro : gruppoDTO.membri) {
                            Target membro = registro.getTarget(idMembro);
                            if (membro != null) gruppo.addTarget(membro);
                        }
                        registro.addTarget(gruppo);
                    }
                }
                System.out.println("Targets caricati con successo.");
            }
        } catch (Exception e) {
            System.err.println("Problema nel caricamento dei targets!");
        }

        // Cronologia
        try (InputStream is = getClass().getResourceAsStream(cartellaConfig + "/cronologia.json");
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            FileCronologiaDTO datiCrono = gson.fromJson(reader, FileCronologiaDTO.class);
            if (datiCrono != null && datiCrono.eventi != null) {
                for (EventoDTO dto : datiCrono.eventi) {
                    List<TransizioneStato> transizioni = new ArrayList<>();
                    
                    if (dto.transizioni != null) {
                        for (TransizioneStatoDTO tDto : dto.transizioni) {
                            transizioni.add(new TransizioneStato(tDto.param, tDto.oldVal, tDto.newVal, tDto.idDispositivo));
                        }
                    }
                    Evento e = new Evento(
                            dto.source, 
                            dto.sourceTimestamp, 
                            dto.tipo, 
                            dto.idTarget, 
                            transizioni,
                            dto.timestamp 
                        );
                        
                        c.addEvento(e);
                }
                System.out.println("Cronologia caricata con successo.");
            }
        } catch (Exception e) {
            System.err.println("Nessun file cronologia.json trovato o errore di lettura.");
        }
        
        // registroSequenze
        try (InputStream is = getClass().getResourceAsStream(cartellaConfig + "/sequenze.json");
             Reader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            FileSequenzeDTO datiSequenze = gson.fromJson(reader, FileSequenzeDTO.class);
            if (datiSequenze != null && datiSequenze.sequenze != null) {
                for (SequenzaDTO seqDto : datiSequenze.sequenze) {
                    
                    SequenzaComandi sequenza = new SequenzaComandi(seqDto.id, seqDto.displayName);
                    
                    if (seqDto.comandi != null) {
                        for (ComandoDTO cmdDto : seqDto.comandi) {
                            sequenza.addComando(new ComandoSingolo(cmdDto.param, cmdDto.valore));
                        }
                    }
                    
                    registroSeq.addSequenza(sequenza);
                }
                System.out.println("Sequenze caricate con successo.");
            }
        } catch (Exception e) {
            System.err.println("Nessun file sequenze.json trovato o errore di lettura.");
        }
        
        // ArchivioReports
        //....
        
        // MotoreRoutines
        //....
        
    }

    // DTO per RegistroTargets
    private static class FileTargetsDTO {
        List<DispositivoDTO> dispositivi;
        List<GruppoDTO> gruppi;
    }
    private static class DispositivoDTO {
        String id, displayName, indirizzoRete;
        double potenza;
        DescrizioneDTO descrizione;
    }
    private static class DescrizioneDTO {
        String marca, tipo, modello;
        List<ParametroDTO> parametri;
    }
    private static class ParametroDTO {
        String nome, tipo, unitaMisura, min, max;
        boolean readOnly;
        List<String> valoriAmmessi;
    }
    private static class GruppoDTO {
        String id, displayName;
        boolean predefinito, stanza;
        List<String> membri;
    }

    // DTO per cronologia
    private static class FileCronologiaDTO {
        List<EventoDTO> eventi;
    }
    private static class EventoDTO {
        String source;
        long sourceTimestamp;
        long timestamp;
        String tipo;
        String idTarget;
        List<TransizioneStatoDTO> transizioni;
    }
    private static class TransizioneStatoDTO {
        String param, oldVal, newVal, idDispositivo;
    }
    // dto per le sequenze
    public class FileSequenzeDTO {
        public List<SequenzaDTO> sequenze;
    }
    public class SequenzaDTO {
        public String id;
        public String displayName;
        public List<ComandoDTO> comandi;
    }
    public class ComandoDTO {
        public String param;
        public String valore;
    }
}