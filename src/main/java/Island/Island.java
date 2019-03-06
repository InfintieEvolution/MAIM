package Island;

import AIS.AIS;
import AIS.Antibody;

import java.util.*;


public class Island {

    // population size - implicit through ais?
    private AIS ais;
    private double migrationRate;
    private double migrationFrequency;
    private ArrayList<IslandConnection> islandConnections;
    private int islandId;
    private double localMigrationRate;
    private Comparator<Antibody> migrationSelectionComparator;
    private int numberOfMigrants;
    Random random = new Random();


    public Island(AIS ais, double migrationRate, double migrationFrequency, int islandId) {
        this.ais = ais;
        this.migrationRate = migrationRate;
        this.migrationFrequency = migrationFrequency;
        this.islandConnections = new ArrayList<>();
        this.islandId = islandId;
        this.localMigrationRate = ((double) migrationRate/1000) * ais.getPopulationSize();
        this.numberOfMigrants = (int) (migrationRate * ais.getPopulationSize());

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
            ArrayList<Antibody> copyList = ais.getAntibodyMap().get(label);
            copyList.sort(migrationSelectionComparator);
            if(migrationRate > copyList.size()){
                antibodyHashMap.put(label, copyList);
            }else {
                ArrayList<Antibody> selectedAntibodies = new ArrayList<Antibody>(copyList.subList(0, (int)migrationRate*100));
                antibodyHashMap.put(label, selectedAntibodies);
            }
        }
        return antibodyHashMap;
    }

    public HashMap<String, ArrayList<Antibody>> sendMigrantsBasedOnPopulationSize(AIS ais) {
        Set<String> labels = ais.getAntibodyMap().keySet();
        HashMap<String, ArrayList<Antibody>>  antibodyHashMap = new HashMap<>();
        for (String label : labels){
            ArrayList<Antibody> copyList = ais.getAntibodyMap().get(label);
            copyList.sort(migrationSelectionComparator);
            if(this.localMigrationRate > copyList.size()){
                antibodyHashMap.put(label, copyList);
            }else {
                ArrayList<Antibody> selectedAntibodies = new ArrayList<Antibody>(copyList.subList(0, (int)this.localMigrationRate));
                antibodyHashMap.put(label, selectedAntibodies);
            }
        }
        return antibodyHashMap;
    }

    public ArrayList<Antibody> sendAllRandom(AIS ais){
        ArrayList<Antibody>  antibodyList = new ArrayList<Antibody>();

        for (int i = 0; i <this.numberOfMigrants; i++){
            int someRandomInteger = random.nextInt((ais.getAntibodies().length - 1) + 1);
            antibodyList.add(ais.getAntibodies()[someRandomInteger]);
        }
        return antibodyList;
    }


    /**
     * Removes the { migrationRate } weakest antibodies for each class
     * @param ais Current Island AIS
     */
    public void removeAntibodies(AIS ais) {
        Set<String> labels = ais.getAntibodyMap().keySet();
        for (String label : labels) {
            ArrayList<Antibody> antibodies = ais.getAntibodyMap().get(label);
            antibodies.sort(migrationSelectionComparator); // Sort the antibodies

            if((migrationRate) > antibodies.size()){
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList(0, antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }else{
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList((antibodies.size()-((int)migrationRate*100)), antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }
        }
    }

    public void removeAntibodiesBasedOnMigrationRate(AIS ais){
        Set<String> labels = ais.getAntibodyMap().keySet();
        for (String label : labels) {
            ArrayList<Antibody> antibodies = ais.getAntibodyMap().get(label);
            antibodies.sort(migrationSelectionComparator); // Sort the antibodies

            if((migrationRate*ais.getPopulationSize()) > antibodies.size()){
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList(0, antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }else{
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList((antibodies.size()-((int) migrationRate*ais.getPopulationSize())), antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }
        }
    }

    /**
     * Removes the { migrationRate } weakest antibodies for each class
     * @param ais Current Island AIS
     */
    public void removeAntibodies2(AIS ais) {
        Set<String> labels = ais.getAntibodyMap().keySet();
        for (String label : labels) {
            ArrayList<Antibody> antibodies = ais.getAntibodyMap().get(label);
            antibodies.sort(migrationSelectionComparator); // Sort the antibodies

            if(this.localMigrationRate > antibodies.size()){
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList(0, antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }else{
                ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList((antibodies.size()-(int) localMigrationRate), antibodies.size())); // Create new list with mRate worst antibodies
                ais.getAntibodyMap().get(label).removeAll(selectedForRemoval); // remove from original list

            }
        }
    }

    public void receiveRandom(Island sendingIsland) {
        removeAntibodiesBasedOnMigrationRate(this.getAis());

        ArrayList<Antibody> receivingAntibodies = sendingIsland.sendAllRandom(sendingIsland.getAis());
        for (Antibody antibody : receivingAntibodies){
            antibody.setAis(this.getAis());
            this.getAis().getAntibodyMap().get(antibody.getLabel()).add(antibody);
        }
    }


    public void receive(Island sendingIsland){
        // remove bad antibodies
        this.removeAntibodies(this.getAis());
//        this.removeAntibodies2(this.getAis());
        //Adds new antibodies with higher fitness
//        HashMap<String, ArrayList<Antibody>> receivingAntibodies = sendingIsland.sendMigrantsBasedOnPopulationSize(sendingIsland.getAis());
        HashMap<String, ArrayList<Antibody>> receivingAntibodies = sendingIsland.sendMigrants(sendingIsland.getAis());
        for (String label : receivingAntibodies.keySet()){
            for (Antibody antibody : receivingAntibodies.get(label)){
                antibody.setAis(this.getAis());
                this.getAis().getAntibodyMap().get(label).add(antibody);
            }
        }
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
