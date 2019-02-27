package Island;

import AIS.AIS;
import AIS.Antibody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
        this.islandConnections = new ArrayList<>();
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


    /**
     * Selects the best antibody for each label for migration, and removes it from current ais
     * @param ais - This islands AIS
     * @return HashMap<String, Antibody> with the highest fitness from this ais
     */
    public HashMap<String, Antibody> sendMigrants(AIS ais) {
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

    /**
     * Removes the weakest antibodies for each class
     * @param ais Current Island AIS
     */
    public void removeAntibodies(AIS ais) {
        Set<String> labels = ais.getAntibodyMap().keySet();
        for (String label : labels) {
            Antibody weakestAntibody = null;
            ArrayList<Antibody> antibodies = ais.getAntibodyMap().get(label);
            for (Antibody antibody : antibodies) {
                if (antibody.getFitness() < weakestAntibody.getFitness() || weakestAntibody == null) {
                    weakestAntibody = antibody;
                }
            }
            ais.getAntibodyMap().get(label).remove(weakestAntibody);
        }
    }

    public void receive(Island sendingIsland){
        // remove bad antibodies
        this.removeAntibodies(this.getAis());

        //Adds new antibodies with higher fitness
        HashMap<String, Antibody> receivingAntibodies = sendingIsland.sendMigrants(sendingIsland.getAis());
        for (Map.Entry<String, Antibody> entry : receivingAntibodies.entrySet()){
            this.getAis().getAntibodyMap().get(entry.getKey()).add(entry.getValue());
        }
    }
}
