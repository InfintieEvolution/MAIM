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
        var receivingAntibodies = fromIsland.sendMigrants();
        for (Antibody antibody: receivingAntibodies) {
            this.ais.getAntibodyMap().get(antibody.getLabel()).add(antibody);
        }
    }

    public void select(int islandIntegrationCount){
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
        final Antibody[] survivors = new Antibody[subPopulationList.size()*((allIslands.size()-islandIntegrationCount)/allIslands.size())];

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
                subPopulations[islandCount].get(label).addAll(allIslands.get(i).getAis().getAntibodyMap().get(label));
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
            double accuracyTest = AIS.vote(this.ais.getAntigenMap(),subPopulation);
            double accuracyValidation = AIS.vote(this.ais.getAntigenValidationMap(),subPopulation);
            double accuracy;
            if(accuracyValidation > 0.0){
                accuracy = (accuracyTest + accuracyValidation)/2;
            }else{
                accuracy = accuracyTest;
            }
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

    public void select2(){
        HashMap<String,ArrayList<Antibody>>[] newAntibodyMaps = new HashMap[allIslands.size()];

        for(int i=0;i<allIslands.size();i++){
            HashMap<String,ArrayList<Antibody>> newAntibodyMap = new HashMap<>();

            for(String label: ais.getLabels()){
                newAntibodyMap.put(label,new ArrayList<>());
                newAntibodyMap.get(label).addAll(ais.getAntibodyMap().get(label));
            }

            for(String label: allIslands.get(i).getAis().getAntibodyMap().keySet()){
                newAntibodyMap.get(label).addAll(allIslands.get(i).getAis().getAntibodyMap().get(label));
            }
            newAntibodyMaps[i] = AIS.fitnessProportionateSelection(newAntibodyMap,ais.getPopulationSize(),ais.getLabels());
        }

        for(int i=0;i<newAntibodyMaps.length;i++){
            double accuracyTest = AIS.vote(this.ais.getAntigenMap(),newAntibodyMaps[i]);
            double accuracyValidation = AIS.vote(this.ais.getAntigenValidationMap(),newAntibodyMaps[i]);
            double accuracy;
            if(accuracyValidation > 0.0){
                accuracy = (accuracyTest + accuracyValidation)/2;
            }else{
                accuracy = accuracyTest;
            }
            if(accuracy > currentAccuracy){
                this.ais.setAntibodyMap(newAntibodyMaps[i]);
                currentAccuracy = accuracy;
            }
        }
    }

    public void select3(){
        HashMap<String,ArrayList<Antibody>> newAntibodyMap = new HashMap<>();

        for(String label: ais.getLabels()){
            newAntibodyMap.put(label,new ArrayList<>());
            newAntibodyMap.get(label).addAll(ais.getAntibodyMap().get(label));
        }
        for(int i=0;i<allIslands.size();i++){

            for(String label: allIslands.get(i).getAis().getAntibodyMap().keySet()){
                newAntibodyMap.get(label).addAll(allIslands.get(i).getAis().getAntibodyMap().get(label));
            }
        }

        newAntibodyMap = AIS.fitnessProportionateSelection(newAntibodyMap,ais.getPopulationSize(),ais.getLabels());

        double accuracyTest = AIS.vote(this.ais.getAntigenMap(),newAntibodyMap);
        double accuracyValidation = AIS.vote(this.ais.getAntigenValidationMap(),newAntibodyMap);
        double accuracy;
        if(accuracyValidation > 0.0){
            accuracy = (accuracyTest + accuracyValidation)/2;
        }else{
            accuracy = accuracyTest;
        }
        if(accuracy > currentAccuracy){
            this.ais.setAntibodyMap(newAntibodyMap);
            currentAccuracy = accuracy;
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
