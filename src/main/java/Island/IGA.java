package Island;
import AIS.AIS;
import Algorithm.DataSet;
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
    private final boolean MASTERISLAND;
    private MasterIsland masterIsland;
    /**
     *
     * @param numberOfIslands Number of islands
     * @param populationSize Total population size
     * @param iterations Total iterations to run
     * @param migrationFrequency Determines how ofter migration should occur
     * @@param migrationRate How many individuals to migrate
     */
    public IGA(int numberOfIslands, int populationSize, int iterations, double migrationFrequency, double migrationRate, boolean masterIsland) {
        this.numberOfIslands = numberOfIslands;
        this.populationSize = populationSize;
        this.iterations = iterations;
        this.currentIterations = 0;
        this.migrationFrequency = migrationFrequency;
        this.migrationRate = migrationRate;
        islands = new ArrayList<>();
        islandConnections = new ArrayList<>();
        this.migrationTime = iterations / (this.migrationFrequency * this.iterations);
        this.MASTERISLAND = masterIsland;
    }

    public void initialize(DataSet dataSet, double mutationRate, int numberOfTournaments, int iterations){

        // create islands
        for(int i=0; i < numberOfIslands; i++){
            AIS ais = new AIS(dataSet.trainingSet,dataSet.featureMap,dataSet.labels,dataSet.antigenMap, (this.populationSize/this.numberOfIslands), mutationRate, numberOfTournaments, iterations);
            this.islands.add(new Island(ais, migrationRate, migrationFrequency, i));
        }
        if(islands.size() > 1){

            // connect islands
            for (int i=0; i < islands.size(); i++){
                if (i == 0){
                    Island sendToIsland = this.islands.get(i+1);
                    Island receiveFromIsland = this.islands.get(this.islands.size()-1);
                    this.islandConnections.add(new IslandConnection(sendToIsland, receiveFromIsland));

                }else if(i == this.islands.size()-1){
                    Island sendToIsland = this.islands.get(0);
                    Island receiveFromIsland = this.islands.get(i-1);
                    this.islandConnections.add(new IslandConnection(sendToIsland, receiveFromIsland));

                }else{
                    Island sendToIsland = this.islands.get(i+1);
                    Island receiveFromIsland = this.islands.get(i-1);
                    this.islandConnections.add(new IslandConnection(sendToIsland, receiveFromIsland));
                }
            }
        }

        if(this.MASTERISLAND) {
             this.masterIsland = new MasterIsland(
                    new AIS(dataSet.trainingSet,dataSet.featureMap,dataSet.labels,dataSet.antigenMap, this.populationSize, mutationRate, numberOfTournaments, iterations),
                    migrationRate,
                    migrationFrequency,
                    this.islands
            );
            System.out.println("MasterIsland Created");
        }
    }

    public boolean migrate() {
        this.currentIterations++;
        // if it's time for migration do so, else something fancy.
        if(this.currentIterations >= migrationTime){
            for (IslandConnection islandConnection : this.islandConnections){
                islandConnection.getReceiveFromIsland().receiveRandom(islandConnection.getSendToIsland());
            }
            this.currentIterations = 0;

            return true;
        }
        return false;
    }

    public void migrateMaster(){
        if (this.MASTERISLAND){
            this.masterIsland.removeAntibodies();
            for(Island island : this.islands) {
                this.masterIsland.receive(island);
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
