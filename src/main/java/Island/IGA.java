package Island;
import AIS.AIS;
import Algorithm.DataSet;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class IGA {

    private int numberOfIslands;
    private int populationSize;
    private int iterations;
    private int currentIterations;
    private double migrationFrequency;
    private double migrationRate;
    private double migrationTime;
    private ArrayList<Island> islands;
    private ArrayList<IslandConnection> islandConnections;

    /**
     *
     * @param numberOfIslands Number of islands
     * @param populationSize Total population size
     * @param iterations Total iterations to run
     * @param migrationFrequency Determines how ofter migration should occur
     * @@param migrationRate How many individuals to migrate
     */
    public IGA(int numberOfIslands, int populationSize, int iterations, double migrationFrequency, double migrationRate) {
        this.numberOfIslands = numberOfIslands;
        this.populationSize = populationSize;
        this.iterations = iterations;
        this.currentIterations = 0;
        this.migrationFrequency = migrationFrequency;
        this.migrationRate = migrationRate;
        islands = new ArrayList<>();
        islandConnections = new ArrayList<>();
        this.migrationTime = iterations / (this.migrationFrequency * this.iterations);
    }

    public void initialize(DataSet dataSet, double mutationRate, int numberOfTournaments, int iterations){

        // create islands
        for(int i=0; i < numberOfIslands; i++){
            if(i==0){ // if master
                AIS ais = new AIS(dataSet.trainingSet,dataSet.featureMap,dataSet.labels,dataSet.antigenMap, this.populationSize, mutationRate, numberOfTournaments, iterations);
                this.islands.add(new Island(ais, migrationRate, migrationFrequency, i));

            }else{
                AIS ais = new AIS(dataSet.trainingSet,dataSet.featureMap,dataSet.labels,dataSet.antigenMap, (this.populationSize/(this.numberOfIslands-1)), mutationRate, numberOfTournaments, iterations);
                this.islands.add(new Island(ais, migrationRate, migrationFrequency, i));
            }
        }
        if(islands.size() > 1){
            Island masterIsland = this.islands.get(0);
            // connect islands
            for (int i=0; i < islands.size(); i++){
                // Define Master Island as island 0
                if (i == 0){
//                    Island sendToIsland = this.islands.get(i+1);
//                    Island receiveFromIsland = this.islands.get(this.islands.size()-1);
                    this.islandConnections.add(new IslandConnection(null, null, masterIsland)); // masterIsland should only receive

                }else if(i == 1){ // island 1
                    Island sendToIsland = this.islands.get(i+1); // send to next island
                    Island receiveFromIsland = this.islands.get(this.islands.size()-1); // receive from last island and not island 0 (masterIsland)
                    this.islandConnections.add(new IslandConnection(sendToIsland, receiveFromIsland, masterIsland));


                }else if(i == this.islands.size()-1) { // last island
                    Island sendToIsland = this.islands.get(1); // send to island 1
                    Island receiveFromIsland = this.islands.get(i-1); // receive from island before
                    this.islandConnections.add(new IslandConnection(sendToIsland, receiveFromIsland, masterIsland));

                }else{ // all middle islands
                    Island sendToIsland = this.islands.get(i+1); // send to next island
                    Island receiveFromIsland = this.islands.get(i-1); // receive from previous island
                    this.islandConnections.add(new IslandConnection(sendToIsland, receiveFromIsland, masterIsland));
                }
            }
        }
    }

    public boolean migrate() {
        this.currentIterations++;
        // if it's time for migration do so, else something fancy.
        if(this.currentIterations >= migrationTime){
            for (IslandConnection islandConnection : this.islandConnections){
                if(islandConnection.getReceiveFromIsland() != null){
                    islandConnection.getReceiveFromIsland().receiveRandom(islandConnection.getSendToIsland());
//                    System.out.println("RandomIsland=" + (islandConnection.getReceiveFromIsland().getAis().getAntibodies().size()));
//                    System.out.println("MasterIsland=" + getMasterIsland().getAis().getAntibodies().size());
                }
            }
            this.currentIterations = 0;

            return true;
        }
        return false;
    }

    public void migrateToMaster() {
        for(Island island : getIslands()){
            if(island != getMasterIsland()){
                System.out.println("island id "+ island.getIslandId());
//                System.out.println("MasterIslandSize=" + getMasterIsland().getAis().getAntibodies().size());
                getMasterIsland().masterReceive(island);
            }
        }
    }

    public int getNumberOfIslands() {
        return numberOfIslands;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public double getMigrationFrequency() {
        return migrationFrequency;
    }

    public Island getMasterIsland(){
        return this.getIsland(0);
    }

    public ArrayList<Island> getIslands() {
        return islands;
    }

    public Island getIsland(int islandId) {
        return this.islands.get(islandId);
    }

    public ArrayList<AIS> getAllAIS(){
        ArrayList<AIS> ais = new ArrayList<>();
        for (Island island : getIslands()){

            ais.add(island.getAis());
        }
        return ais;
    }
    public ArrayList<IslandConnection> getIslandConnections() {
        return islandConnections;
    }

    @Override
    public String toString() {
        return "IGA{" +
                "numberOfIslandConnections=" + islandConnections.size() +
                ", numberOfIslands=" + numberOfIslands +
                ", populationSize=" + populationSize +
                ", islandConnections=" + islandConnections +
                '}';
    }
}
