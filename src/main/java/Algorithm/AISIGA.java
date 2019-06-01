package Algorithm;

import AIS.AIS;
import AIS.Antigen;
import AIS.Antibody;
import GUI.GUI;
import Island.IGA;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class AISIGA extends Application {

    // private TextField populationSizeInput = new TextField();
    private boolean running = false;
    private GUI gui;
    private AIS ais;
    private ArrayList<AIS> allAIS;
    private boolean radiusPlot = true;
    public static final int PROCESSORS = Runtime.getRuntime().availableProcessors();

    public int f = 0;
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
                    boolean masterValidation,
                    boolean plotSolution,
                    boolean globalSharingFactor) {

        if(k > 1){

            HashSet<String> excludeDatasets = new HashSet<>();
            excludeDatasets.add("abalone.data");
            excludeDatasets.add("crabs.data");
            excludeDatasets.add("spirals.txt");
            excludeDatasets.add("bupa.data");

            for(String datasetName:gui.dataSetLabelIndexes.keySet()){

                if(excludeDatasets.contains(datasetName)){
                    continue;
                }
                int[] islandNumbers = new int[]{1,2,3,4,5,6,7,8,9,10,11,12};

                for(int islandNumber:islandNumbers){

                    for(int i=0; i<10;i++){

                        if (islandNumber==1){

                            this.testStuff(k,
                                    iterations,
                                    populationSize,
                                    mutationRate,
                                    numberOfTournaments,
                                    datasetName,
                                    gui.dataSetLabelIndexes.get(datasetName),
                                    migrationFrequency,
                                    islandNumber,
                                    migrationRate,
                                    false,
                                    islandIntegrationCount,
                                    pcaDimensions,
                                    validationSplit,
                                    masterValidation,
                                    globalSharingFactor);
                        }else{

                            this.testStuff(k,
                                    iterations,
                                    populationSize,
                                    mutationRate,
                                    numberOfTournaments,
                                    datasetName,
                                    gui.dataSetLabelIndexes.get(datasetName),
                                    migrationFrequency,
                                    islandNumber,
                                    migrationRate,
                                    masterIsland,
                                    islandIntegrationCount,
                                    pcaDimensions,
                                    validationSplit,
                                    masterValidation,
                                    globalSharingFactor);
                        }
                    }
                }
            }
            /*this.validateAccuracies(k,
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
                    masterValidation,
                    globalSharingFactor);*/

        }else{
        this.running = true;
        gui.startButton.setDisable(true);
        gui.iterationTextField.setDisable(true);
        gui.setBox.setDisable(true);
        gui.stopButton.setDisable(false);

        DataSet dataSet = new DataSet("./DataSets/" + dataSetName, trainingTestSplit,validationSplit, labelIndex,pcaDimensions);

        IGA iga = new IGA(numberOfIslands, populationSize, iterations, migrationFrequency, migrationRate, masterIsland,globalSharingFactor);
        iga.initialize(dataSet, mutationRate, numberOfTournaments, iterations, masterValidation);

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
        gui.createStatisticGraph(iterations,islandCount,masterIsland);

        ArrayList< ArrayList<Antibody>>[] antibodyGenerations = new ArrayList[islandCount]; //contains the antibody population for each iteration.
        ArrayList<Double>[] antibodyGenerationAccuracies = new ArrayList[islandCount]; //contains the population accuracies over each iteration.
        int[] bestIterations = new int[islandCount];

        if(plotSolution){

            for(int j=0;j<islandCount;j++){
                antibodyGenerations[j] = new ArrayList<ArrayList<Antibody>>();
                antibodyGenerationAccuracies[j] = new ArrayList<>();
            }
        }
            Thread aisThread = new Thread(() -> {
            for (int i = 0; i < iterations; i++) {
                if (!this.getRunning()) {
                    break;
                }
                boolean migrate = iga.migrate();

                for (int j = 0; j < allAIS.size(); j++) {
                    AIS someAIS = allAIS.get(j);
                    double accuracy = AIS.vote(someAIS.getAntigenMap(), someAIS.getAntibodies(),null);
                    if(plotSolution) {
                        antibodyGenerationAccuracies[j].add(accuracy);
                        antibodyGenerations[j].add(AIS.copy(someAIS.getAntibodies()));
                    }
                    someAIS.setCurrentAccuracy(accuracy);
                    gui.addIteration(accuracy, migrate,j);
                    if (accuracy >= someAIS.getBestAccuracy()) {
                        bestIterations[j] = i;
                        gui.setBestAccuracy(accuracy,j);
                        someAIS.setBestAccuracy(accuracy);
                        someAIS.setBestIteration(i);
                    }
                }

                if(iga.hasMaster()){
                    iga.migrateMaster(islandIntegrationCount);
                    double accuracy = iga.getMasterIsland().getCurrentAccuracy();

                    if(plotSolution) {
                        antibodyGenerationAccuracies[islandCount - 1].add(accuracy);
                        antibodyGenerations[islandCount - 1].add(AIS.copy(iga.getMasterIsland().getAis().getAntibodies()));
                    }
                    gui.addIteration(accuracy, migrate,islandCount-1);
                    if (accuracy >= iga.getMasterIsland().getAis().getBestAccuracy() && iga.getMasterIsland().isPopulationChanged()) {
                        bestIterations[islandCount-1] = i;
                        gui.setBestAccuracy(accuracy,islandCount-1);
                        iga.getMasterIsland().getAis().setBestAccuracy(accuracy);
                        iga.getMasterIsland().getAis().setBestIteration(i);
                    }
                }

                for (int j = 0; j < allAIS.size(); j++) {
                        AIS someAIS = allAIS.get(j);
                        someAIS.iterate();
                }
                /*final ExecutorService executor = Executors.newFixedThreadPool(PROCESSORS);
                //System.out.println(PROCESSORS);
                for (int j = 0; j < allAIS.size(); j++) {
                    final int index = j;
                    executor.execute(() -> {
                        AIS someAIS = allAIS.get(index);
                        someAIS.iterate();
                    });
                }
                executor.shutdown();
                while (!executor.isTerminated()) {
                }*/
            }

            if(iga.hasMaster()){
                iga.migrateMaster(islandIntegrationCount);
                double accuracy = iga.getMasterIsland().getCurrentAccuracy();
                if(plotSolution) {
                    antibodyGenerations[islandCount - 1].add(iga.getMasterIsland().getAis().getAntibodies());
                    antibodyGenerationAccuracies[islandCount - 1].add(AIS.vote(iga.getMasterIsland().getCombinedAntigenMap(), iga.getMasterIsland().getAis().getAntibodies(),null));
                }
                gui.addIteration(accuracy, false,islandCount-1);
                if (accuracy >= iga.getMasterIsland().getAis().getBestAccuracy() && iga.getMasterIsland().isPopulationChanged()) {
                    bestIterations[islandCount-1] = antibodyGenerations[islandCount-1].size()-1;
                    gui.setBestAccuracy(accuracy,islandCount-1);
                    iga.getMasterIsland().getAis().setBestAccuracy(accuracy);
                    iga.getMasterIsland().getAis().setBestIteration(antibodyGenerations[islandCount-1].size()-1);
                }
            }
            for (int j = 0; j < allAIS.size(); j++) {
                AIS someAIS = allAIS.get(j);
                double accuracy = AIS.vote(someAIS.getAntigenMap(), someAIS.getAntibodies(),null);
                if(plotSolution) {
                    antibodyGenerationAccuracies[j].add(accuracy);
                    antibodyGenerations[j].add(AIS.copy(someAIS.getAntibodies()));
                }
                someAIS.setCurrentAccuracy(accuracy);
                gui.addIteration(accuracy, false,j);
                if (accuracy >= someAIS.getBestAccuracy()) {
                    bestIterations[j] = antibodyGenerations[j].size()-1;
                    gui.setBestAccuracy(accuracy,j);
                    someAIS.setBestAccuracy(accuracy);
                    someAIS.setBestIteration(antibodyGenerations[j].size()-1);
                }
            }

            Platform.runLater(() -> {
                gui.startButton.setDisable(false);
                gui.startButton.requestFocus();
                gui.iterationTextField.setDisable(false);
                gui.setBox.setDisable(false);
                gui.stopButton.setDisable(true);
                if(plotSolution) {
                    this.gui.setAntibodyGenerations(antibodyGenerations,bestIterations,antibodyGenerationAccuracies,dataSet.antigenMap,dataSet.testAntigenMap, radiusPlot);
                    if(masterIsland){
                        this.gui.createSolutionGraph(iga.getMasterIsland().getAis().getFeatureMap(), iga.getMasterIsland().getAis().getAntibodies());
                    }else{
                        this.gui.createSolutionGraph(iga.getIsland(0).getAis().getFeatureMap(), iga.getIsland(0).getAis().getAntibodies());
                    }
                    gui.drawSolution(dataSet.testAntigenMap, antibodyGenerations[islandCount-1].get(bestIterations[islandCount-1]), 0.0, radiusPlot);
                    gui.setBestAccuracyIteration(antibodyGenerationAccuracies[islandCount-1].get(bestIterations[islandCount-1]), bestIterations[islandCount-1]);


                }
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
                                   boolean masterValidation,
                                   boolean globalSharingFactor){


        this.running = true;
        gui.startButton.setDisable(true);
        gui.iterationTextField.setDisable(true);
        gui.stopButton.setDisable(false);
        Random random = new Random();
        double[] accuracies = new double[k];
        DataSet dataSet = new DataSet("./DataSets/" + dataSetName, 0.0,0.0, labelIndex,pcaDimensions);
        HashMap<String,ArrayList<Antigen>>[] dataSetSplits = DataSet.splitDataSet(k,dataSet.antigenMap);
        gui.createStatisticGraph(k-1,1,false);

        Thread aisThread = new Thread(() -> {

            double totalBestAccuracy = 0.0;
            for(int j=0; j<accuracies.length;j++){
                if (!this.getRunning()) {
                    break;
                }
            HashMap<String,ArrayList<Antigen>> testSetMap = dataSetSplits[j];
            HashMap<String,ArrayList<Antigen>> trainingSetMap = new HashMap<>();
            HashMap<String,ArrayList<Antigen>> validationSetMap = new HashMap<>();
            int validationAntigenCount = 0;
            int totalTrainingAntigen = 0;
            int trainingAntigenCount = 0;
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
                            validationAntigenCount ++;
                        }else{
                            trainingSetMap.get(label).add(antigen);
                            antigenArrayList.add(antigen);
                            trainingAntigenCount ++;
                        }
                        totalTrainingAntigen++;
                    }
                }
            }
            /*while (validationAntigenCount < (int)(totalTrainingAntigen*validationSplit)){
                Antigen antigen = antigenArrayList.remove(random.nextInt(antigenArrayList.size()));
                trainingSetMap.get(antigen.getLabel()).remove(antigen);
                validationSetMap.get(antigen.getLabel()).add(antigen);
                validationAntigenCount++;
                trainingAntigenCount--;
            }
            while (trainingAntigenCount < (int)(totalTrainingAntigen*(1-validationSplit))){
                String randomLabel = dataSet.labels.get(random.nextInt(dataSet.labels.size()));
                Antigen antigen = validationSetMap.get(randomLabel).remove(random.nextInt(validationSetMap.get(randomLabel).size()));
                trainingSetMap.get(randomLabel).add(antigen);
                antigenArrayList.add(antigen);
                trainingAntigenCount++;
                validationAntigenCount--;
            }*/

            Antigen[] antigens = new Antigen[antigenArrayList.size()];
            antigens = antigenArrayList.toArray(antigens);

            dataSet.setTrainingSet(antigens);
            dataSet.setAntigenMap(trainingSetMap);
            dataSet.setValidationAntigenMap(validationSetMap);

            IGA iga = new IGA(numberOfIslands, populationSize, iterations, migrationFrequency, migrationRate, masterIsland,globalSharingFactor);
            iga.initialize(dataSet, mutationRate, numberOfTournaments, iterations, masterValidation);

            if(iga.hasMaster()){
                this.ais = iga.getMasterIsland().getAis();
            }
            else{
                    this.ais = iga.getIsland(0).getAis(); // new
            }

            this.allAIS = iga.getAllAIS();

            //ArrayList<HashMap<String, ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>();
                ArrayList<Antibody> bestGeneration = new ArrayList<>();
                for (int i = 0; i < iterations; i++) {
                    if (!this.getRunning()) {
                        break;
                    }
                    gui.setCurrentIteration(i+1,0);

                    double accuracy;
                    iga.migrate();

                    for (int m = 0; m < allAIS.size(); m++) {
                        AIS someAIS = allAIS.get(m);
                        accuracy = AIS.vote(someAIS.getAntigenMap(), someAIS.getAntibodies(),null);
                        someAIS.setCurrentAccuracy(accuracy);
                    }

                    if(iga.hasMaster()){
                        iga.migrateMaster(islandIntegrationCount);
                        accuracy = iga.getMasterIsland().getCurrentAccuracy();
                    }else{
                        accuracy = AIS.vote(ais.getAntigenMap(), ais.getAntibodies(),null);
                    }
                    //antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));

                    if (accuracy >= ais.getBestAccuracy()) {
                        ais.setBestAccuracy(accuracy);
                        ais.setBestIteration(i);
                        bestGeneration = AIS.copy(ais.getAntibodies());
                    }

                    for (int m = 0; m < allAIS.size(); m++) {
                        AIS someAIS = allAIS.get(m);
                        someAIS.iterate();
                    }
                }

            double acc;
            if(iga.hasMaster()){
                iga.migrateMaster(islandIntegrationCount);
                acc =iga.getMasterIsland().getCurrentAccuracy();
            }else{
                acc = AIS.vote(ais.getAntigenMap(), ais.getAntibodies(),null);
            }

            if (acc >= ais.getBestAccuracy()) {
                ais.setBestAccuracy(acc);
                ais.setBestIteration(iterations);
            }

            //antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));

            //HashMap<String, ArrayList<Antibody>> bestGeneration =  antibodyGenerations.get(ais.getBestIteration());

            double accuracy =AIS.vote(testSetMap,bestGeneration,null);
            gui.addIteration(accuracy, false,0);
            accuracies[j] = accuracy;

            if(accuracy >= totalBestAccuracy){
                totalBestAccuracy = accuracy;
                gui.setBestAccuracy(accuracy,0);
            }
            double accuracySum = 0.0;
            int accuracyCount = 0;
            for(double ac: accuracies){
                if(ac != 0.0){
                    accuracySum += ac;
                    accuracyCount++;
                }
            }
            gui.setAverageAccuracy(accuracySum/accuracyCount,0);
        }

        Platform.runLater(() -> {
            gui.startButton.setDisable(false);
            gui.startButton.requestFocus();
            gui.iterationTextField.setDisable(false);
            gui.stopButton.setDisable(true);
        });
        });

        aisThread.start();
    }

    public void testStuff(int k,
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
                                   boolean masterValidation,
                                   boolean globalSharingFactor){


        this.running = true;
        gui.startButton.setDisable(true);
        gui.iterationTextField.setDisable(true);
        gui.stopButton.setDisable(false);
        Random random = new Random();
        double[] accuracies = new double[k];
        DataSet dataSet = new DataSet("./DataSets/" + dataSetName, 0.0,0.0, labelIndex,pcaDimensions);
        int totalAntigenPopulation = dataSet.testSet.length + dataSet.trainingSet.length + dataSet.validationSet.length;
        HashMap<String,ArrayList<Antigen>>[] dataSetSplits = DataSet.splitDataSet(k,dataSet.antigenMap);

            double totalBestAccuracy = 0.0;
        long startTime = System.nanoTime();
        for(int j=0; j<accuracies.length;j++){
                if (!this.getRunning()) {
                    break;
                }
                HashMap<String,ArrayList<Antigen>> testSetMap = dataSetSplits[j];
                HashMap<String,ArrayList<Antigen>> trainingSetMap = new HashMap<>();
                HashMap<String,ArrayList<Antigen>> validationSetMap = new HashMap<>();
                int validationAntigenCount = 0;
                int totalTrainingAntigen = 0;
                int trainingAntigenCount = 0;
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
                                validationAntigenCount ++;
                            }else{
                                trainingSetMap.get(label).add(antigen);
                                antigenArrayList.add(antigen);
                                trainingAntigenCount ++;
                            }
                            totalTrainingAntigen++;
                        }
                    }
                }
            /*while (validationAntigenCount < (int)(totalTrainingAntigen*validationSplit)){
                Antigen antigen = antigenArrayList.remove(random.nextInt(antigenArrayList.size()));
                trainingSetMap.get(antigen.getLabel()).remove(antigen);
                validationSetMap.get(antigen.getLabel()).add(antigen);
                validationAntigenCount++;
                trainingAntigenCount--;
            }
            while (trainingAntigenCount < (int)(totalTrainingAntigen*(1-validationSplit))){
                String randomLabel = dataSet.labels.get(random.nextInt(dataSet.labels.size()));
                Antigen antigen = validationSetMap.get(randomLabel).remove(random.nextInt(validationSetMap.get(randomLabel).size()));
                trainingSetMap.get(randomLabel).add(antigen);
                antigenArrayList.add(antigen);
                trainingAntigenCount++;
                validationAntigenCount--;
            }*/

                Antigen[] antigens = new Antigen[antigenArrayList.size()];
                antigens = antigenArrayList.toArray(antigens);

                dataSet.setTrainingSet(antigens);
                dataSet.setAntigenMap(trainingSetMap);
                dataSet.setValidationAntigenMap(validationSetMap);

                IGA iga = new IGA(numberOfIslands, totalAntigenPopulation, iterations, migrationFrequency, migrationRate, masterIsland,globalSharingFactor);
                iga.initialize(dataSet, mutationRate, numberOfTournaments, iterations, masterValidation);

                if(iga.hasMaster()){
                    this.ais = iga.getMasterIsland().getAis();
                }
                else{
                    this.ais = iga.getIsland(0).getAis(); // new
                }

                this.allAIS = iga.getAllAIS();

                //ArrayList<HashMap<String, ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>();
                ArrayList<Antibody> bestGeneration = new ArrayList<>();
                for (int i = 0; i < iterations; i++) {
                    if (!this.getRunning()) {
                        break;
                    }

                    double accuracy;
                    iga.migrate();

                    for (int m = 0; m < allAIS.size(); m++) {
                        AIS someAIS = allAIS.get(m);
                        accuracy = AIS.vote(someAIS.getAntigenMap(), someAIS.getAntibodies(),null);
                        someAIS.setCurrentAccuracy(accuracy);
                    }

                    if(iga.hasMaster()){
                        iga.migrateMaster(islandIntegrationCount);
                        accuracy = iga.getMasterIsland().getCurrentAccuracy();
                    }else{
                        accuracy = AIS.vote(ais.getAntigenMap(), ais.getAntibodies(),null);
                    }
                    //antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));

                    if (accuracy >= ais.getBestAccuracy()) {
                        ais.setBestAccuracy(accuracy);
                        ais.setBestIteration(i);
                        bestGeneration = AIS.copy(ais.getAntibodies());
                    }

                    for (int m = 0; m < allAIS.size(); m++) {
                        AIS someAIS = allAIS.get(m);
                        someAIS.iterate();
                    }
                }

                double acc;
                if(iga.hasMaster()){
                    iga.migrateMaster(islandIntegrationCount);
                    acc =iga.getMasterIsland().getCurrentAccuracy();
                }else{
                    acc = AIS.vote(ais.getAntigenMap(), ais.getAntibodies(),null);
                }

                if (acc >= ais.getBestAccuracy()) {
                    ais.setBestAccuracy(acc);
                    ais.setBestIteration(iterations);
                }

                //antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));

                //HashMap<String, ArrayList<Antibody>> bestGeneration =  antibodyGenerations.get(ais.getBestIteration());

                double accuracy =AIS.vote(testSetMap,bestGeneration,null);
                accuracies[j] = accuracy;

                if(accuracy >= totalBestAccuracy){
                    totalBestAccuracy = accuracy;
                }
                double accuracySum = 0.0;
                int accuracyCount = 0;
                for(double ac: accuracies){
                    if(ac != 0.0){
                        accuracySum += ac;
                        accuracyCount++;
                    }
                }
                double averageAccuracy = accuracySum/accuracyCount;

                int scale = (int) Math.pow(10, 4);
                averageAccuracy = (double) Math.round(averageAccuracy * scale) / scale;
                if(j == accuracies.length-1){
                    long endTime = System.nanoTime();
                    long time = endTime - startTime;
                    long timeInMilli = NANOSECONDS.toMillis(time);
                    double timeInSeconds = (double) timeInMilli/1000;

                    String stuff = dataSetName+","+timeInSeconds+","+averageAccuracy+"\n";
                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter("./"+numberOfIslands+"-class-distributed.csv", true));
                        writer.append(stuff);

                        writer.close();
                        this.f += 1;
                        System.out.println("line written: " + String.valueOf(f));

                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }
            }

            Platform.runLater(() -> {
                gui.startButton.setDisable(false);
                gui.startButton.requestFocus();
                gui.iterationTextField.setDisable(false);
                gui.stopButton.setDisable(true);
            });

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