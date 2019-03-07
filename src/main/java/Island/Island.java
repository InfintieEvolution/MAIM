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
    private Comparator<Antibody> migrationSelectionComparator;
    private int numberOfMigrants;
    Random random = new Random();


    public Island(AIS ais, double migrationRate, double migrationFrequency, int islandId) {
        this.ais = ais;
        this.migrationRate = migrationRate;
        this.migrationFrequency = migrationFrequency;
        this.islandConnections = new ArrayList<>();
        this.islandId = islandId;
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


    public ArrayList<Antibody> sendToMaster(AIS ais) {
        ArrayList<Antibody> allAntibodies = new ArrayList<Antibody>(ais.getAntibodies());
        allAntibodies.sort(migrationSelectionComparator);
        return new ArrayList<Antibody>(allAntibodies.subList(0, numberOfMigrants));
    }


    public ArrayList<Antibody> sendAllRandom(AIS ais){
        ArrayList<Antibody> antibodyList = new ArrayList<>();
        for (int i = 0; i <this.numberOfMigrants; i++){
            int someRandomInteger = random.nextInt((ais.getAntibodies().size() - 1) + 1);
            antibodyList.add(ais.getAntibodies().get(someRandomInteger));
        }
        return antibodyList;
    }


    /**
     * Removes the { migrationRate } weakest antibodies for each class
     * @param ais Current Island AIS
     */
    public void removeAntibodies(AIS ais) {

        ArrayList<Antibody> antibodies = ais.getAntibodies();
        antibodies.sort(migrationSelectionComparator);
        ArrayList<Antibody> selectedForRemoval = new ArrayList<Antibody>(antibodies.subList((antibodies.size()-numberOfMigrants), antibodies.size())); // Create new list with mRate worst antibodies
        for(Antibody ab : selectedForRemoval){
            antibodies.remove(ab);
        }
    }

    public void removeRandomAntibodies(AIS ais) {
        ArrayList<Antibody> antibodies = ais.getAntibodies();
        for (int i = 0; i <this.numberOfMigrants; i++){
            int someRandomInteger = random.nextInt((antibodies.size() - 1) + 1);
            antibodies.remove(someRandomInteger);
        }
    }


    public void receiveRandom(Island sendingIsland) {
        removeAntibodies(this.getAis());

        ArrayList<Antibody> receivingAntibodies = sendingIsland.sendAllRandom(sendingIsland.getAis());
        for (Antibody antibody : receivingAntibodies){
            var newAntibody = new Antibody(antibody.getFeatures(), antibody.getRadius(), antibody.getLabel(), antibody.getAntigens(), this.getAis());
            newAntibody.setAccuracy(antibody.getAccuracy());
            this.getAis().getAntibodyMap().get(antibody.getLabel()).add(newAntibody);
        }
    }

    public void masterReceive(Island sendingIsland) {
//        this.removeRandomAntibodies(this.getAis());
        this.removeAntibodies(this.getAis());

        ArrayList<Antibody> receivingAntibodies = sendingIsland.sendToMaster(sendingIsland.getAis());
        for (Antibody antibody : receivingAntibodies) {
            var newAntibody = new Antibody(antibody.getFeatures(), antibody.getRadius(), antibody.getLabel(), antibody.getAntigens(), this.getAis());
            this.getAis().getAntibodyMap().get(antibody.getLabel()).add(newAntibody);
        }

    }


    public void receive(Island sendingIsland){
        // remove bad antibodies
        this.removeAntibodies(this.getAis());
        HashMap<String, ArrayList<Antibody>> receivingAntibodies = sendingIsland.sendMigrants(sendingIsland.getAis());
        for (String label : receivingAntibodies.keySet()){
            for (Antibody antibody : receivingAntibodies.get(label)){
                antibody.setAis(this.getAis());
                this.getAis().getAntibodyMap().get(label).add(antibody);
            }
        }
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

    public int getIslandId() {
        return islandId;
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
