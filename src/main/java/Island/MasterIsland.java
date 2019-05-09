package Island;

import AIS.AIS;
import AIS.Antibody;
import AIS.Antigen;
import Algorithm.DataSet;

import javax.xml.crypto.Data;
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
    private HashMap<String, ArrayList<Antigen>> combinedAntigenMap;
    private HashMap<String, ArrayList<Antigen>> validationAntigenMap;
    private HashMap<String, ArrayList<Antigen>> trainingAntigenMap;
    private boolean populationChanged = false;
    private HashMap<String,ArrayList<Antibody>> combinedBestMap;
    private HashMap<Island,Double> accuracies;
    private HashMap<String,ArrayList<Antibody>>[] bestGenerations;

    public MasterIsland(AIS ais, double migrationRate, double migrationFrequency, ArrayList<Island> allIslands) {
        this.ais = ais;
        this.migrationRate = migrationRate;
        this.migrationFrequency = migrationFrequency;
        this.numberOfMigrants = (int) (migrationRate * ais.getPopulationSize());
        this.allIslands = allIslands;
        this.currentAccuracy = 0.0;
        combinedAntigenMap = new HashMap<>();
        validationAntigenMap = new HashMap<>();
        trainingAntigenMap = new HashMap<>();

        combinedBestMap = new HashMap<>();
        accuracies = new HashMap<>();
        bestGenerations = new HashMap[allIslands.size()];

        for(int j = 0; j < allIslands.size(); j++) {
            accuracies.put(allIslands.get(j),0.0);
        }
            for(String label: ais.getLabels()){
            combinedAntigenMap.put(label,new ArrayList<>());
            validationAntigenMap.put(label,new ArrayList<>());
            trainingAntigenMap.put(label,new ArrayList<>());
            if(this.ais.getAntigenMap().containsKey(label)) {
            combinedAntigenMap.get(label).addAll(this.ais.getAntigenMap().get(label));
            trainingAntigenMap.get(label).addAll(this.ais.getAntigenMap().get(label));
            }if(this.ais.getAntigenValidationMap().containsKey(label)){
                combinedAntigenMap.get(label).addAll(this.ais.getAntigenValidationMap().get(label));
                validationAntigenMap.get(label).addAll(this.ais.getAntigenValidationMap().get(label));
            }
        }

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
        var receivingAntibodies = fromIsland.sendMigrants();
        for (Antibody antibody: receivingAntibodies) {
            this.ais.getAntibodyMap().get(antibody.getLabel()).add(antibody);
        }
    }

    public void select(int islandIntegrationCount){
        if(islandIntegrationCount == allIslands.size()){
            incorporateAllIslands();
            return;
        }
        this.ais.setIteration(this.ais.getIteration()+1);
        ArrayList<Antibody> subPopulationList = new ArrayList<>();

        for(String label: this.ais.getAntibodyMap().keySet()){
            subPopulationList.addAll(this.ais.getAntibodyMap().get(label));
        }

        double integrationModifier = 1.25*((double)this.ais.getIteration()/this.ais.getMaxIterations())*islandIntegrationCount;

        islandIntegrationCount += (int)integrationModifier;
        if(islandIntegrationCount > allIslands.size()){
            islandIntegrationCount = allIslands.size(); //make sure integration count is no too large
        }
        final Antibody[] survivors = new Antibody[(int)(subPopulationList.size()*((double)(allIslands.size()-islandIntegrationCount)/allIslands.size()))];

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
                /*Antibody antibody1 = new Antibody(antibody.getFeatures(),antibody.getRadius(),antibody.getLabel(),this.getAis().getAntigens(),this.getAis());
                subPopulation.get(antibody1.getLabel()).add(antibody1);*/

                subPopulation.get(antibody.getLabel()).add(antibody);
            }

        }

        int islandCount = 0;
        int bestIslandIndex = 0;
        double bestIslandAccuracy = 0.0;
        for(int i=0; i<allIslands.size();i++){
            int integratedIslands = 1;
            HashMap<String,ArrayList<Antibody>> subPopulation = subPopulations[islandCount];

            for(String label: allIslands.get(i).getAis().getAntibodyMap().keySet()){
                subPopulation.get(label).addAll(allIslands.get(i).getAis().getAntibodyMap().get(label));
            }

            //add islands to population, but only add islands that have not been added before
            HashSet<Integer> islandIndexes = new HashSet<>();
            islandIndexes.add(i);
            while (integratedIslands < islandIntegrationCount){
                int islandIndex = random.nextInt(allIslands.size());
                while (islandIndexes.contains(islandIndex)){
                    islandIndex = random.nextInt(allIslands.size());
                }

                Island island = allIslands.get(islandIndex);
                for(String label: island.getAis().getAntibodyMap().keySet()){
                    /*for(Antibody antibody: island.getAis().getAntibodyMap().get(label)){
                        Antibody antibody1 = new Antibody(antibody.getFeatures(),antibody.getRadius(),antibody.getLabel(),this.getAis().getAntigens(),this.getAis());
                        subPopulations[islandCount].get(label).add(antibody1);
                    }*/
                    subPopulations[islandCount].get(label).addAll(island.getAis().getAntibodyMap().get(label));
                }
                islandIndexes.add(islandIndex);
                integratedIslands++;
            }

            /*for(Antigen antigen:this.getAis().getAntigens()){
                antigen.setConnectedAntibodies(new ArrayList<>());
                antigen.setTotalInteraction(0.0);
                antigen.getInteractionMap().put(getAis(),0.0);
            }

            for(String label: subPopulation.keySet()){
                for(Antibody antibody:subPopulation.get(label)){
                    antibody.setConnectedAntigens();
                }
            }
            //calculate fitness after connections has been set
            for(String label: subPopulation.keySet()) {
                for(Antibody antibody:subPopulation.get(label)) {
                    antibody.calculateFitness();
                }
            }*/
            /*HashMap<String,ArrayList<Antigen>>[] antigenList = DataSet.splitDataSet(3,this.ais.getAntigenMap());

            double accuracySum = 0.0;
            for(HashMap<String,ArrayList<Antigen>> antigens: antigenList){
                accuracySum += AIS.vote(antigens,subPopulation);

            }*/
            //double accuracy = accuracySum/antigenList.length;

            /*double accuracyTest = AIS.vote(this.ais.getAntigenMap(),subPopulation);
            double accuracyValidation = AIS.vote(this.ais.getAntigenValidationMap(),subPopulation);
            double accuracy;
            if(accuracyValidation > 0.0){
                accuracy = (accuracyTest + accuracyValidation)/2;
            }else{
                accuracy = accuracyTest;
            }*/
            double accuracy = AIS.vote(combinedAntigenMap,subPopulation,null);
            if(accuracy > bestIslandAccuracy){
                bestIslandIndex = islandCount;
                bestIslandAccuracy = accuracy;
            }
            islandCount++;
        }

        if(bestIslandAccuracy >= currentAccuracy){
            this.ais.setAntibodyMap(subPopulations[bestIslandIndex]);
            currentAccuracy = bestIslandAccuracy;
            populationChanged = true;
        }else{
            populationChanged = false;
        }
    }

    public void incorporateAllIslands(){
        populationChanged = false;

        /*boolean newBest = false;
        for(int j = 0; j < allIslands.size(); j++) {
            double currentAccuracy = allIslands.get(j).getAis().getCurrentAccuracy();
            double currentBestAccuracy = accuracies.get(allIslands.get(j));
            if(currentAccuracy >= currentBestAccuracy){
                accuracies.put(allIslands.get(j),currentAccuracy);
                bestGenerations[j] = allIslands.get(j).getAis().getAntibodyMap();
                newBest = true;
            }
        }
        if(newBest){
            combinedBestMap = new HashMap<>();

            for(String label: ais.getLabels()){
                combinedBestMap.put(label,new ArrayList<>());
            }
            for(int j = 0; j < allIslands.size(); j++){
                HashMap<String,ArrayList<Antibody>> islandBestMap = bestGenerations[j];
                for(String label: islandBestMap.keySet()){
                    combinedBestMap.get(label).addAll(islandBestMap.get(label));
                }
            }

            double accuracy = AIS.vote(combinedAntigenMap,combinedBestMap,null);
            if(accuracy >= currentAccuracy){
                this.ais.setAntibodyMap(combinedBestMap);
                currentAccuracy = accuracy;
                populationChanged = true;
            }
        }*/


        HashMap<String,ArrayList<Antibody>> population = new HashMap<>();
        for(String label: this.ais.getLabels()){
            population.put(label,new ArrayList<>());
        }

        for(int i=0; i<allIslands.size();i++){
            for(String label: allIslands.get(i).getAis().getAntibodyMap().keySet()){
                population.get(label).addAll(allIslands.get(i).getAis().getAntibodyMap().get(label));
            }
        }

        //double accuracy1 = AIS.vote(trainingAntigenMap,population,null);
        //double accuracy2 = AIS.vote(validationAntigenMap,population,null);

        double accuracy = AIS.vote(validationAntigenMap,population,null);
        //double accuracy = (accuracy1 + accuracy2)/2;
        if(accuracy >= currentAccuracy){
            this.ais.setAntibodyMap(population);
            currentAccuracy = accuracy;
            populationChanged = true;
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

    public boolean isPopulationChanged() {
        return populationChanged;
    }

    public void setPopulationChanged(boolean populationChanged) {
        this.populationChanged = populationChanged;
    }

    public HashMap<String, ArrayList<Antibody>>[] getBestGenerations() {
        return bestGenerations;
    }

    public void setBestGenerations(HashMap<String, ArrayList<Antibody>>[] bestGenerations) {
        this.bestGenerations = bestGenerations;
    }

    public HashMap<String, ArrayList<Antigen>> getCombinedAntigenMap() {
        return combinedAntigenMap;
    }

    public void setCombinedAntigenMap(HashMap<String, ArrayList<Antigen>> combinedAntigenMap) {
        this.combinedAntigenMap = combinedAntigenMap;
    }
}
