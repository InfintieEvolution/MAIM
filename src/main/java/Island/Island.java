package Island;

import AIS.AIS;
import AIS.Antibody;

import java.lang.reflect.AnnotatedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;


public class Island {

    // population size - implicit through ais?
    private AIS ais;
    private double migrationRate;
    private double migrationFrequency;
    private ArrayList<IslandConnection> islandConnections;

    public Island(AIS ais, double migrationRate, double migrationFrequency) {
        this.ais = ais;
        this.migrationRate = migrationRate;
        this.migrationFrequency = migrationFrequency;
    }

    public AIS getAis() {
        return this.ais;
    }

    public double getMigrationRate() {
        return this.migrationRate;
    }

    public double getMigrationFrequency() {
        return this.migrationFrequency;
    }


//    TODO: Remember to remove from this AIS
    public HashMap<String, Antibody> selectForMigration(AIS ais) {
        Set<String> labels = ais.getAntibodyMap().keySet();
        HashMap<String, Antibody>  antibodyHashMap = new HashMap<>();
        for (String label : labels){
            Antibody bestAntibody = null;
            for(Antibody antibody: ais.getAntibodyMap().get(label)){
                if(antibody.getFitness() > bestAntibody.getFitness() || bestAntibody == null) {
                    bestAntibody = antibody;
                }
            }
            antibodyHashMap.put(label, bestAntibody);
        }
        return antibodyHashMap;
    }

//    TODO: Remember to remove from this AIS
    public HashMap<String, Antibody> selectForReplacement(AIS ais) {
        Set<String> labels = ais.getAntibodyMap().keySet();
        HashMap<String, Antibody> antibodyHashMap = new HashMap<>();

        for (String label : labels) {
            Antibody weakestAntibody = null;
            for (Antibody antibody : ais.getAntibodyMap().get(label)) {
                if (antibody.getFitness() < weakestAntibody.getFitness() || weakestAntibody == null) {
                    weakestAntibody = antibody;
                }
                antibodyHashMap.put(label, weakestAntibody);
            }
        }
        return antibodyHashMap;
    }


    public void send(Island receivingIslnad){

    }

    public void receive(Island sendingIslnad){

    }

    public void migrate() {

    }


}
