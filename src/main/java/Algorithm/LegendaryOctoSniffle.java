package Algorithm;

import AIS.AIS;
import AIS.Antigen;
import AIS.Antibody;
import GUI.GUI;
import GUI.SolutionGraph;
import Island.IGA;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class LegendaryOctoSniffle extends Application {

    // private TextField populationSizeInput = new TextField();
    private boolean running = false;
    private GUI gui;
    private AIS ais;
    private ArrayList<AIS> allAIS;

    @Override
    public void start(Stage primaryStage) throws Exception {
        gui = new GUI(primaryStage, this);
    }

    public void run(int iterations, int populationSize, double mutationRate, int numberOfTournaments,
            String dataSetName, int labelIndex, double trainingTestSplit, double migrationFrequency,
            int numberOfIslands, double migrationRate, boolean masterIsland,int k) {

        if(k > 1){
            this.validateAccuracies(k, iterations, populationSize, mutationRate, numberOfTournaments,
                    dataSetName, labelIndex, migrationFrequency,
                    numberOfIslands, migrationRate, masterIsland);
        }else{
        this.running = true;
        gui.startButton.setDisable(true);
        gui.iterationTextField.setDisable(true);
        gui.stopButton.setDisable(false);

        DataSet dataSet = new DataSet("./DataSets/" + dataSetName, trainingTestSplit, labelIndex);

        IGA iga = new IGA(numberOfIslands, populationSize, iterations, migrationFrequency, migrationRate, masterIsland);
        iga.initialize(dataSet, mutationRate, numberOfTournaments, iterations);

        if(iga.hasMaster()){
            this.ais = iga.getMasterIsland().getAis();
        }
        else{
            this.ais = iga.getIsland(0).getAis(); // new
        }
                                              // AIS(dataSet.trainingSet,dataSet.featureMap,dataSet.labels,dataSet.antigenMap,populationSize,
                                              // mutationRate,numberOfTournaments,iterations);
        this.allAIS = iga.getAllAIS();
        gui.createStatisticGraph(iterations);

        HashMap<String, ArrayList<Antigen>> testSetMap = Antigen.createAntigenMap(dataSet.testSet);
        ArrayList<HashMap<String, ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>();
        ArrayList<Double> antibodyGenerationAccuracies = new ArrayList<>();

            Thread aisThread = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                if (!this.getRunning()) {
                    break;
                }
                boolean migrate = iga.migrate();

                if(iga.hasMaster()){
                    iga.migrateMaster();
                }

                antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));

                double accuracy;
                if(iga.hasMaster()){
                    accuracy = iga.getMasterIsland().getCurrentAccuracy();
                }else{
                    accuracy = AIS.vote(ais.getAntigenMap(), ais.getAntibodyMap());
                }
                antibodyGenerationAccuracies.add(accuracy);

                gui.addIteration(accuracy, migrate);

                if (accuracy > ais.getBestAccuracy()) {
                    gui.setBestAccuracy(accuracy);
                    ais.setBestAccuracy(accuracy);
                    ais.setBestItreation(i);
                }

                for (int j = 0; j < allAIS.size(); j++) {
                    allAIS.get(j).iterate();
                }
            }

            antibodyGenerations.add(ais.getAntibodyMap());
            antibodyGenerationAccuracies.add(AIS.vote(ais.getAntigenMap(),ais.getAntibodyMap()));

            Platform.runLater(() -> {
                gui.startButton.setDisable(false);
                gui.startButton.requestFocus();
                gui.iterationTextField.setDisable(false);
                gui.stopButton.setDisable(true);
                this.gui.setAntibodyGenerations(antibodyGenerations, ais.getAntigenMap(), testSetMap,
                        antibodyGenerations.get(ais.getBestItreation()),antibodyGenerationAccuracies);
                this.gui.createSolutionGraph(ais.getFeatureMap(), ais.getAntibodyMap());
                gui.drawSolution(testSetMap, antibodyGenerations.get(ais.getBestItreation()),0.0);
                gui.setBestAccuracyIteration(ais.getBestAccuracy(), ais.getBestItreation());
            });
        });
        aisThread.start();
        }
    }

    public void validateAccuracies(int k, int iterations, int populationSize, double mutationRate, int numberOfTournaments,
                                   String dataSetName, int labelIndex, double migrationFrequency,
                                   int numberOfIslands, double migrationRate, boolean masterIsland){

        this.running = true;
        gui.startButton.setDisable(true);
        gui.iterationTextField.setDisable(true);
        gui.stopButton.setDisable(false);

        double[] accuracies = new double[k];
        DataSet dataSet = new DataSet("./DataSets/" + dataSetName, 0.0, labelIndex);
        HashMap<String,ArrayList<Antigen>>[] dataSetSplits = DataSet.splitDataSet(k,dataSet.antigenMap);
        gui.createStatisticGraph(k-1);

        Thread aisThread = new Thread(() -> {

            double totalBestAccuracy = 0.0;
            for(int j=0; j<accuracies.length;j++){
                if (!this.getRunning()) {
                    break;
                }
            HashMap<String,ArrayList<Antigen>> testSetMap = dataSetSplits[j];
            HashMap<String,ArrayList<Antigen>> trainingSetMap = new HashMap<>();
            ArrayList<Antigen> antigenArrayList = new ArrayList<>();
            for(String label: dataSet.labels){
                trainingSetMap.put(label,new ArrayList<>());
            }

            for(int n=0; n<dataSetSplits.length;n++){
                if(n == j){
                    continue;
                }
                for(String label: dataSetSplits[n].keySet()){
                    trainingSetMap.get(label).addAll(dataSetSplits[n].get(label));
                    antigenArrayList.addAll(dataSetSplits[n].get(label));
                }
            }
            Antigen[] antigens = new Antigen[antigenArrayList.size()];
            antigens = antigenArrayList.toArray(antigens);

            dataSet.setTrainingSet(antigens);
            dataSet.setAntigenMap(trainingSetMap);

            IGA iga = new IGA(numberOfIslands, populationSize, iterations, migrationFrequency, migrationRate, masterIsland);
            iga.initialize(dataSet, mutationRate, numberOfTournaments, iterations);

            if(iga.hasMaster()){
                this.ais = iga.getMasterIsland().getAis();
            }
            else{
                this.ais = iga.getIsland(0).getAis(); // new
            }

            this.allAIS = iga.getAllAIS();

            ArrayList<HashMap<String, ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>();
                for (int i = 0; i < iterations; i++) {
                    if (!this.getRunning()) {
                        break;
                    }
                    for (int m = 0; m < allAIS.size(); m++) {
                        allAIS.get(m).iterate();
                    }
                    iga.migrate();

                    if(iga.hasMaster()){
                        iga.migrateMaster();
                    }

                    antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));
                    double accuracy;
                    if(iga.hasMaster()){
                        accuracy = iga.getMasterIsland().getCurrentAccuracy();
                    }else{
                        accuracy = AIS.vote(ais.getAntigenMap(), ais.getAntibodyMap());
                    }

                    if (accuracy > ais.getBestAccuracy()) {
                        ais.setBestAccuracy(accuracy);
                        ais.setBestItreation(i);
                    }
                }

            antibodyGenerations.add(ais.getAntibodyMap());

            HashMap<String, ArrayList<Antibody>> bestGeneration =  antibodyGenerations.get(ais.getBestItreation());

            double accuracy =AIS.vote(testSetMap,bestGeneration);
            gui.addIteration(accuracy, false);
            accuracies[j] = accuracy;

            if(accuracy > totalBestAccuracy){
                totalBestAccuracy = accuracy;
                gui.setBestAccuracy(accuracy);
            }

        }

        double accuracySum = 0.0;
            int accuracyCount = 0;
        for(double accuracy: accuracies){
            if(accuracy != 0.0){
                accuracySum += accuracy;
                accuracyCount++;
            }
        }
        gui.setAverageAccuracy(accuracySum/accuracyCount);

        Platform.runLater(() -> {
            gui.startButton.setDisable(false);
            gui.startButton.requestFocus();
            gui.iterationTextField.setDisable(false);
            gui.stopButton.setDisable(true);
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
    public void stop() {
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