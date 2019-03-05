package Island;

import AIS.AIS;
import AIS.Antibody;

import java.util.*;


public class Island {

    // population size - implicit through ais?
    private AIS ais;
    private int migrationRate;
    private double migrationFrequency;
    private ArrayList<IslandConnection> islandConnections;
    private int islandId;

    private Comparator<Antibody> migrationSelectionComparator;

    public Island(AIS ais, int migrationRate, double migrationFrequency, int islandId) {
        this.ais = ais;
        this.migrationRate = migrationRate;
        this.migrationFrequency = migrationFrequency;
        this.islandConnections = new ArrayList<>();
        this.islandId = islandId;

        migrationSelectionComparator = (o1, o2) -> {
            if (o1.getFitness() > o2.getFitness()) {
                return -1;
            }
            else if (o1.getFitness() < o2.getFitness()) {
                return 1;
            }
            return 0;
        };
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
     * Selects the {migrationRate} best antibodies for each label for migration
     * @param ais - This islands AIS
     * @return HashMap<String, ArrayList<Antibody>> with the highest fitness from this ais
     */
    public HashMap<String, ArrayList<Antibody>> sendMigrants(AIS ais) {
        Set<String> labels = ais.getAntibodyMap().keySet();
        HashMap<String, ArrayList<Antibody>>  antibodyHashMap = new HashMap<>();
        for (String label : labels){
//            Antibody bestAntibody = null;
            ArrayList<Antibody> copyList = ais.getAntibodyMap().get(label);
            copyList.sort(migrationSelectionComparator);
            if(migrationRate > copyList.size()){
                antibodyHashMap.put(label, copyList);
            }else {
                ArrayList<Antibody> selectedAntibodies = new ArrayList<Antibody>(copyList.subList(0, migrationRate));
                antibodyHashMap.put(label, selectedAntibodies);

            }
//            System.out.println("0:= " + ais.getAntibodyMap().get(label).get(0).getFitness() + " 1:= " + ais.getAntibodyMap().get(label).get(1).getFitness() + " 2:= " + ais.getAntibodyMap().get(label).get(2).getFitness());
//            for(Antibody antibody: ais.getAntibodyMap().get(label)){
//                if( bestAntibody == null || antibody.getFitness() > bestAntibody.getFitness()) {
//                    bestAntibody = antibody;
//                }
//            }
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
//            Antibody weakestAntibody = null;
            ArrayList<Antibody> antibodies = ais.getAntibodyMap().get(label);
            antibodies.sort(migrationSelectionComparator); // Sort the antibodies

            if(migrationRate > antibodies.size()){
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList(0, antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }else{
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList((antibodies.size()-migrationRate), antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }

//            for (Antibody antibody : antibodies) {
//                if (weakestAntibody == null || antibody.getFitness() < weakestAntibody.getFitness()) {
//                    weakestAntibody = antibody;
//                }
//            }
//            ais.getAntibodyMap().get(label).remove(weakestAntibody);
        }
    }


    public void receive(Island sendingIsland){
        // remove bad antibodies
        this.removeAntibodies(this.getAis());
        //Adds new antibodies with higher fitness
        HashMap<String, ArrayList<Antibody>> receivingAntibodies = sendingIsland.sendMigrants(sendingIsland.getAis());
        for (String label : receivingAntibodies.keySet()){
            for (Antibody antibody : receivingAntibodies.get(label)){
                this.getAis().getAntibodyMap().get(label).add(antibody);
            }
        }
//        for (Map.Entry<String, Antibody> entry : receivingAntibodies.entrySet()) {
//            this.getAis().getAntibodyMap().get(entry.getKey()).add(entry.getValue());
//        }
    }

    @Override
    public String toString() {
        return "Island{" +
                "islandId=" + islandId +
//                ", AIS best accuracy= " + this.getAis().getBestAccuracy() +
//                ", AIS best accuracy test= " + this.getAis().getBestAccuracyTestSet()+
//                ", AIS best iteration= " + this.getAis().getBestItreation() +
                '}';
    }
}
