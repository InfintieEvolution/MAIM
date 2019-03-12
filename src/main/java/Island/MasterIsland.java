package Island;

import AIS.AIS;
import AIS.Antibody;
import AIS.Antigen;

import java.lang.reflect.Array;
import java.util.*;

public class MasterIsland {

    private AIS ais;
    private double migrationRate;
    private double migrationFrequency;
    private Comparator<Antibody> migrationSelectionComparator;
    private int numberOfMigrants;
    private ArrayList<Island> allIslands;
    Random random = new Random();
    private HashMap<String,ArrayList<Antibody>> recievedAntibodyMap;
    private double currentAccuracy;

    public MasterIsland(AIS ais, double migrationRate, double migrationFrequency, ArrayList<Island> allIslands) {
        this.ais = ais;
        this.migrationRate = migrationRate;
        this.migrationFrequency = migrationFrequency;
        this.numberOfMigrants = (int) (migrationRate * ais.getPopulationSize());
        this.allIslands = allIslands;
        this.currentAccuracy = 0.0;

        this.migrationSelectionComparator = (o1, o2) -> {
            if (o1.getFitness() > o2.getFitness()) {
                return -1;
            }
            else if (o1.getFitness() < o2.getFitness()) {
                return 1;
            }
            return 0;
        };
    }

    public void removeRandomAntibodies() {
        Set<String> labels = this.ais.getAntibodyMap().keySet();
        ArrayList<Antibody>allAntibodies = new ArrayList<>();
        for (String label : labels) {
            allAntibodies.addAll(this.ais.getAntibodyMap().get(label));
        }

        for (int i=0; i < numberOfMigrants; i++) {
            int randomIndex = random.nextInt((allAntibodies.size()-1)+1);
            var antibody = allAntibodies.get(randomIndex);

            this.ais.getAntibodyMap().get(antibody.getLabel()).remove(antibody);
        }
    }

    public void receive(Island fromIsland) {
//        this.removeRandomAntibodies();
        var receivingAntibodies = fromIsland.sendMigrants();
        for (Antibody antibody: receivingAntibodies) {
            this.ais.getAntibodyMap().get(antibody.getLabel()).add(antibody);
        }
    }

    public void receive2(Island fromIsland){
        var receivingAntibodies = fromIsland.sendMigrants();
        for(String label: this.ais.getLabels()){
            recievedAntibodyMap.put(label,new ArrayList<>());
        }

        for (Antibody antibody: receivingAntibodies) {

            //Create a new antibody
            Antibody newAntibody = new Antibody(antibody.getFeatures(),antibody.getRadius(),antibody.getLabel(),antibody.getAntigens(),this.ais);
            newAntibody.setAccuracy(antibody.getAccuracy());
            recievedAntibodyMap.get(antibody.getLabel()).add(newAntibody);
        }
    }

    public void select(){
        ArrayList<Antibody> subPopulationList = new ArrayList<>();

        for(String label: this.ais.getAntibodyMap().keySet()){
            subPopulationList.addAll(this.ais.getAntibodyMap().get(label));
        }
        final Antibody[] survivors = new Antibody[(subPopulationList.size()*((allIslands.size()-1)/allIslands.size()))];

        subPopulationList.sort(migrationSelectionComparator);

        for(int i=0; i<survivors.length;i++){
            survivors[i] = subPopulationList.get(i);
        }

        HashMap<String,ArrayList<Antibody>>[] subPopulations = new HashMap[allIslands.size()];
        for(int i = 0; i<subPopulations.length;i++){
            subPopulations[i] = new HashMap<>();
        }
        for(HashMap<String,ArrayList<Antibody>> subPopulation: subPopulations){
            for(String label: ais.getLabels()){
                subPopulation.put(label,new ArrayList<>());
            }
            for(Antibody antibody: survivors){
                subPopulation.get(antibody.getLabel()).add(antibody);
            }

        }

        int islandCount = 0;
        int bestIslandIndex = 0;
        double bestIslandAccuracy = 0.0;
        for(Island island: allIslands){
            HashMap<String,ArrayList<Antibody>> subPopulation = subPopulations[islandCount];

            for(String label: island.getAis().getAntibodyMap().keySet()){
                subPopulations[islandCount].get(label).addAll(island.getAis().getAntibodyMap().get(label));
            }
            double accuracy = AIS.vote(this.ais.getAntigenMap(),subPopulation);
            if(accuracy > bestIslandAccuracy){
                bestIslandIndex = islandCount;
                bestIslandAccuracy = accuracy;
            }
            islandCount++;
        }

        if(bestIslandAccuracy > currentAccuracy){
            this.ais.setAntibodyMap(subPopulations[bestIslandIndex]);
            currentAccuracy = bestIslandAccuracy;
        }
    }

    public void removeWorstAntibodies() {
        Set<String> labels = this.ais.getAntibodyMap().keySet();
        ArrayList<Antibody> allAntibodies = new ArrayList<>();
        for (String label : labels) {
            allAntibodies.addAll(ais.getAntibodyMap().get(label));
        }
        allAntibodies.sort(migrationSelectionComparator); // Sort the antibodies
        ArrayList<Antibody> worstAntibodies = new ArrayList<>(allAntibodies.subList(allAntibodies.size()-this.numberOfMigrants, allAntibodies.size()));
        for (Antibody antibody : worstAntibodies) {
            this.ais.getAntibodyMap().get(antibody.getLabel()).remove(antibody);
        }
    }


    public AIS getAis() {
        return ais;
    }

    public double getMigrationRate() {
        return migrationRate;
    }

    public double getMigrationFrequency() {
        return migrationFrequency;
    }

    public int getNumberOfMigrants() {
        return numberOfMigrants;
    }

    public HashMap<String, ArrayList<Antibody>> getRecievedAntibodyMap() {
        return recievedAntibodyMap;
    }

    public double getCurrentAccuracy() {
        return currentAccuracy;
    }

    public void setCurrentAccuracy(double currentAccuracy) {
        this.currentAccuracy = currentAccuracy;
    }

    public void setRecievedAntibodyMap(HashMap<String, ArrayList<Antibody>> recievedAntibodyMap) {
        this.recievedAntibodyMap = recievedAntibodyMap;
    }
}
