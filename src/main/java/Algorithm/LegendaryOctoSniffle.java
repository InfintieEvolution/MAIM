package Algorithm;
import AIS.AIS;
import AIS.Antigen;
import AIS.Antibody;
import GUI.GUI;
import GUI.SolutionGraph;
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

        int labelColumn = 0;
        if(dataSetName.equals("iris.data")){
            labelColumn = 4;
        }
        //System.out.println(labelColumn);
        DataSet dataSet = new DataSet("./DataSets/"+dataSetName,trainingTestSplit,labelColumn);
        this.ais = new AIS(dataSet.trainingSet,dataSet.featureMap,dataSet.labels,dataSet.antigenMap,populationSize, mutationRate,numberOfTournaments);

        gui.createStatisticGraph(iterations);

        HashMap<String,ArrayList<Antigen>> testSetMap = Antigen.createAntigenMap(dataSet.testSet);
        ArrayList<HashMap<String,ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>();
        Thread aisThread = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                if(!this.getRunning()){
                    break;
                }
                antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));
                double accuracy = AIS.vote(ais.getAntigenMap(), ais.getAntibodyMap());
                double accuracyTestSet = AIS.vote(testSetMap,ais.getAntibodyMap());
                gui.addIteration(accuracy);

                if(accuracy > ais.getBestAccuracy()){
                    gui.setBestAccuracy(accuracy);
                    ais.setBestAccuracy(accuracy);
                    ais.setBestItreation(i);
                }
                if(accuracyTestSet > ais.getBestAccuracyTestSet()){
                    ais.setBestAccuracyTestSet(accuracyTestSet);
                    ais.setBestIterationTestSet(i);
                }
                ais.iterate();
            }
            antibodyGenerations.add(ais.getAntibodyMap());

            //double accuracy = AIS.vote(testSetMap, ais.getAntibodyMap());
            Platform.runLater(() -> {
                gui.startButton.setDisable(false);
                gui.stopButton.setDisable(true);
                this.gui.createSolutionGraph(ais.getFeatureMap(), ais.getAntibodyMap());
                this.gui.setAntibodyGenerations(antibodyGenerations, ais.getAntigenMap(), testSetMap, antibodyGenerations.get(ais.getBestItreation()));
                gui.drawSolution(testSetMap, antibodyGenerations.get(ais.getBestItreation()));
                gui.setBestAccuracyIteration(ais.getBestAccuracy(),ais.getBestItreation());
            });
        });
        aisThread.start();

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

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public GUI getGui() {
        return gui;
    }

    public void setGui(GUI gui) {
        this.gui = gui;
    }

    public AIS getAis() {
        return ais;
    }

    public void setAis(AIS ais) {
        this.ais = ais;
    }
}