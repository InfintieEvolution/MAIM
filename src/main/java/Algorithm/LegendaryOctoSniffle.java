package Algorithm;
import AIS.AIS;
import AIS.Antigen;
import AIS.Antibody;
import GUI.GUI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;


public class LegendaryOctoSniffle extends Application{

    //private TextField populationSizeInput = new TextField();
    private boolean running = false;
    private GUI gui;
    private AIS ais;

    @Override
    public void start(Stage primaryStage) throws Exception {
        gui = new GUI(primaryStage,this);
    }

    public void run(int iterations, int populationSize, double mutationRate, int numberOfTournaments, String dataSetName, double trainingTestSplit){
        this.running = true;
        gui.startButton.setDisable(true);
        gui.stopButton.setDisable(false);

        DataSet dataSet = new DataSet("./DataSets/"+dataSetName,trainingTestSplit);
        this.ais = new AIS(dataSet.trainingSet,dataSet.antigenMap,populationSize, mutationRate,numberOfTournaments);

        ArrayList<HashMap<String,ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>();

        for(int i=0;i<iterations;i++){
            antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));
            ais.iterate();
        }

        //creating hashmap for testset
        HashMap<String,ArrayList<Antigen>> testSetMap = Antigen.createAntigenMap(dataSet.testSet);

        double accuracy = AIS.vote(testSetMap,ais.getAntibodyMap());

        Platform.runLater(() -> {
            gui.startButton.setDisable(false);
            gui.stopButton.setDisable(true);
            this.gui.setAntibodyGenerations(antibodyGenerations,ais.getAntigenMap(),testSetMap,ais.getAntibodyMap());
            this.gui.createSolutionGraph(ais.getFeatureMap());
            gui.drawSolution(testSetMap,ais.getAntibodyMap());
        });

        gui.startButton.setDisable(false);
    }

    public synchronized boolean getRunning() {
        return running;
    }

    public void stopRunning() {
        running = false;
        gui.startButton.setDisable(false);
        gui.stopButton.setDisable(true);
    }

    @Override
    public void stop(){
        System.exit(0);
    }

    public static void main(String[] args) {
        launch(args);
    }

}