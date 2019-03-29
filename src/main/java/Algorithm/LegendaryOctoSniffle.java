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
    private boolean radiusPlot = true;

    @Override
    public void start(Stage primaryStage) throws Exception {
        gui = new GUI(primaryStage, this);
    }

    public void run(int iterations,
                    int populationSize,
                    double mutationRate,
                    int numberOfTournaments,
                    String dataSetName,
                    int labelIndex,
                    double trainingTestSplit,
                    double migrationFrequency,
                    int numberOfIslands,
                    double migrationRate,
                    boolean masterIsland,
                    int k,
                    int islandIntegrationCount,
                    int pcaDimensions,
                    boolean radiusPlot,
                    double validationSplit,
                    double radiusMultiplier) {

        if(k > 1){
            this.validateAccuracies(k,
                    iterations,
                    populationSize,
                    mutationRate,
                    numberOfTournaments,
                    dataSetName,
                    labelIndex,
                    migrationFrequency,
                    numberOfIslands,
                    migrationRate,
                    masterIsland,
                    islandIntegrationCount,
                    pcaDimensions,
                    validationSplit,
                    radiusMultiplier);

        }else{
        this.running = true;
        gui.startButton.setDisable(true);
        gui.iterationTextField.setDisable(true);
        gui.stopButton.setDisable(false);

        DataSet dataSet = new DataSet("./DataSets/" + dataSetName, trainingTestSplit,validationSplit, labelIndex,pcaDimensions);

        IGA iga = new IGA(numberOfIslands, populationSize, iterations, migrationFrequency, migrationRate, masterIsland);
        iga.initialize(dataSet, mutationRate, numberOfTournaments, iterations, radiusMultiplier);

        if(iga.hasMaster()){
            this.ais = iga.getMasterIsland().getAis();
        }
        else{
            this.ais = iga.getIsland(0).getAis(); // new
        }
                                              // AIS(dataSet.trainingSet,dataSet.featureMap,dataSet.labels,dataSet.antigenMap,populationSize,
                                              // mutationRate,numberOfTournaments,iterations);
        this.allAIS = iga.getAllAIS();
        int islandCount;
        if(masterIsland){
            islandCount = numberOfIslands +1;
        }else{
            islandCount = numberOfIslands;
        }
        gui.createStatisticGraph(iterations,islandCount);

        ArrayList<HashMap<String, ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>(); //contains the antibody population for each iteration.
        ArrayList<Double> antibodyGenerationAccuracies = new ArrayList<>(); //contains the population accuracies over each iteration.

            Thread aisThread = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                if (!this.getRunning()) {
                    break;
                }
                boolean migrate = iga.migrate();

                if(iga.hasMaster()){
                    iga.migrateMaster(islandIntegrationCount);
                }

                antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));

                double accuracy;
                if(iga.hasMaster()){
                    accuracy = iga.getMasterIsland().getCurrentAccuracy();
                }else{
                    accuracy = AIS.vote(ais.getAntigenMap(), ais.getAntibodyMap());
                }
                antibodyGenerationAccuracies.add(accuracy);

                for (int j = 0; j < allAIS.size(); j++) {
                    AIS someAIS = allAIS.get(j);
                    accuracy = AIS.vote(someAIS.getAntigenMap(), someAIS.getAntibodyMap());
                    gui.addIteration(accuracy, migrate,j);

                    if (accuracy > someAIS.getBestAccuracy()) {
                        gui.setBestAccuracy(accuracy,j);
                        someAIS.setBestAccuracy(accuracy);
                        someAIS.setBestItreation(i);
                    }
                }
                if(iga.hasMaster()){
                    accuracy = iga.getMasterIsland().getCurrentAccuracy();
                    gui.addIteration(accuracy, migrate,numberOfIslands);
                    if (accuracy > ais.getBestAccuracy()) {
                        gui.setBestAccuracy(accuracy,numberOfIslands);
                        ais.setBestAccuracy(accuracy);
                        ais.setBestItreation(i);
                    }
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
                this.gui.setAntibodyGenerations(antibodyGenerations, ais.getAntigenMap(), dataSet.testAntigenMap,
                        antibodyGenerations.get(ais.getBestItreation()),antibodyGenerationAccuracies,radiusPlot);
                this.gui.createSolutionGraph(ais.getFeatureMap(), ais.getAntibodyMap());
                gui.drawSolution(dataSet.testAntigenMap, antibodyGenerations.get(ais.getBestItreation()),0.0,radiusPlot);
                gui.setBestAccuracyIteration(ais.getBestAccuracy(), ais.getBestItreation());
            });
        });
        aisThread.start();
        }
    }

    public void validateAccuracies(int k,
                                   int iterations,
                                   int populationSize,
                                   double mutationRate,
                                   int numberOfTournaments,
                                   String dataSetName,
                                   int labelIndex,
                                   double migrationFrequency,
                                   int numberOfIslands,
                                   double migrationRate,
                                   boolean masterIsland,
                                   int islandIntegrationCount,
                                   int pcaDimensions,
                                   double validationSplit,
                                   double radiusMultiplier){


        this.running = true;
        gui.startButton.setDisable(true);
        gui.iterationTextField.setDisable(true);
        gui.stopButton.setDisable(false);

        double[] accuracies = new double[k];
        DataSet dataSet = new DataSet("./DataSets/" + dataSetName, 0.0,0.0, labelIndex,pcaDimensions);
        HashMap<String,ArrayList<Antigen>>[] dataSetSplits = DataSet.splitDataSet(k,dataSet.antigenMap);
        gui.createStatisticGraph(k-1,1);

        Thread aisThread = new Thread(() -> {

            double totalBestAccuracy = 0.0;
            for(int j=0; j<accuracies.length;j++){
                if (!this.getRunning()) {
                    break;
                }
            HashMap<String,ArrayList<Antigen>> testSetMap = dataSetSplits[j];
            HashMap<String,ArrayList<Antigen>> trainingSetMap = new HashMap<>();
            HashMap<String,ArrayList<Antigen>> validationSetMap = new HashMap<>();

            ArrayList<Antigen> antigenArrayList = new ArrayList<>();
            for(String label: dataSet.labels){
                trainingSetMap.put(label,new ArrayList<>());
                validationSetMap.put(label,new ArrayList<>());
            }

            for(int n=0; n<dataSetSplits.length;n++){
                if(n == j){
                    continue;
                }
                for(String label: dataSetSplits[n].keySet()){
                    for(Antigen antigen: dataSetSplits[n].get(label)){
                        double p = Math.random();
                        if(p < validationSplit){
                            validationSetMap.get(label).add(antigen);
                        }else{
                            trainingSetMap.get(label).add(antigen);
                            antigenArrayList.add(antigen);
                        }
                    }
                }
            }
            Antigen[] antigens = new Antigen[antigenArrayList.size()];
            antigens = antigenArrayList.toArray(antigens);

            dataSet.setTrainingSet(antigens);
            dataSet.setAntigenMap(trainingSetMap);
            dataSet.setValidationAntigenMap(validationSetMap);

            IGA iga = new IGA(numberOfIslands, populationSize, iterations, migrationFrequency, migrationRate, masterIsland);
            iga.initialize(dataSet, mutationRate, numberOfTournaments, iterations, radiusMultiplier);

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
                        iga.migrateMaster(islandIntegrationCount);
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
            gui.addIteration(accuracy, false,0);
            accuracies[j] = accuracy;

            if(accuracy > totalBestAccuracy){
                totalBestAccuracy = accuracy;
                gui.setBestAccuracy(accuracy,0);
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
        gui.setAverageAccuracy(accuracySum/accuracyCount,0);

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