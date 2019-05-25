package GUI;

import AIS.Antigen;
import AIS.Antibody;
import Algorithm.AISIGA;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;

public class GUI extends BorderPane {

    private final static HashMap<String, Integer> dataSetLabelIndexes = new HashMap<>();
    static {
        dataSetLabelIndexes.put("iris.data", 4);
        dataSetLabelIndexes.put("wine.data", 0);
        dataSetLabelIndexes.put("ionosphere.data", 34);
        dataSetLabelIndexes.put("glass.data", 9);
        dataSetLabelIndexes.put("abalone.data", 0);
        dataSetLabelIndexes.put("crabs.data", 0);
        dataSetLabelIndexes.put("sonar.all-data.txt", 60);
        dataSetLabelIndexes.put("diabetes.csv",8);
        dataSetLabelIndexes.put("spirals.txt",2);
        dataSetLabelIndexes.put("heart.dat",13);
        dataSetLabelIndexes.put("breastCancer.csv",9);
        dataSetLabelIndexes.put("bupa.data",6);

    }
    private final ChoiceBox<String> dataSetBox = new ChoiceBox<>(FXCollections.observableArrayList("iris.data", "wine.data", "ionosphere.data", "glass.data", "crabs.csv", "abalone.data","sonar.all-data.txt","diabetes.csv","spirals.txt","heart.dat","breastCancer.csv","bupa.data"));
    private int labelIndex;

    private final Stage primaryStage;
    private final AISIGA AISIGA;
    private StatisticGraph statisticGraph;

    private SolutionGraph solutionGraph;
    public final Button startButton = new Button("Start");
    public final Button stopButton = new Button("Stop");

    private final TextField inputIterations = new TextField("1000");
    private final TextField inputPopulationSize = new TextField("1000");
    private final TextField inputMutationRate = new TextField("0.8");
    private final TextField inputNumberOfTournaments = new TextField("5");
    private final TextField inputDataSetSplit = new TextField("0.1");
    private final TextField inputValidationSplit = new TextField("0.3");
    public final TextField iterationTextField = new TextField();
    private final TextField inputMigrationFrequency = new TextField("0.1");
    private final TextField inputNumberOfIslands = new TextField("4");
    private final TextField inputMigrationRate = new TextField("0.1");
    private final TextField islandIntegrationCount = new TextField("4");
    private final TextField inputK = new TextField("0");
    private final TextField radiusMultiplier = new TextField("0.0");
    private final TextField pca = new TextField("0");

    private CheckBox masterIslandCheckBox = new CheckBox("MasterIsland");
    private CheckBox radiusCheckBox = new CheckBox("Plot radius");
    private CheckBox plotSolutionCheckBox = new CheckBox("Plot solution");
    private CheckBox globalSharingFactorCheckBox = new CheckBox("Global Sharing Factor");

    private final FlowPane graphPane = new FlowPane();
    private final ScrollPane scrollPane = new ScrollPane();

    public ArrayList<StatisticGraph> graphs = new ArrayList<>();

    private final int sceneWidth = 1550;
    private final int sceneHeight = 1050;
    private final int solutionGraphWidth = 800;
    private final int solutionGraphHeight = 800;
    private final int statisticGraphWidth = 1100;
    private final int statisticGraphHeight = 800;

    private HBox menu;
    private VBox menuWrapper;
    public ChoiceBox<String> setBox = new ChoiceBox<>();

    public GUI(Stage primaryStage, AISIGA AISIGA){

        super();
        this.primaryStage = primaryStage;
        this.AISIGA = AISIGA;
        final Scene scene = new Scene(this, sceneWidth, sceneHeight);
        primaryStage.setScene(scene);
        primaryStage.setTitle("AISIGA");
        primaryStage.getIcons().add(new Image("file:Images/icon2.PNG"));

        //make sure when selecting the data sets, the index of the label on each row is also set
        dataSetBox.getSelectionModel().selectedIndexProperty().addListener((observableValue, number, number2) -> {
            labelIndex = dataSetLabelIndexes.getOrDefault(dataSetBox.getItems().get((int) number2), 0);
        });

        iterationTextField.setPrefWidth(40);
        menuWrapper = new VBox(5);
        menuWrapper.setPadding(new Insets(5,0,5,0));
        dataSetBox.setValue("iris.data");
        dataSetBox.setPrefWidth(150);
        masterIslandCheckBox.setSelected(true);
        plotSolutionCheckBox.setSelected(true);
        radiusCheckBox.setSelected(true);
        menu = new HBox(5);
        //menu.setPadding(new Insets(5,0,10,0));
        menu.setAlignment(Pos.CENTER);
        menu.getChildren().addAll(startButton, stopButton);
        menuWrapper.getChildren().addAll(menu);
        setTop(menuWrapper);
        masterIslandCheckBox.setMinWidth(100);
        radiusCheckBox.setMinWidth(100);
        plotSolutionCheckBox.setMinWidth(100);
        VBox options = new VBox(10);
        options.setPadding(new Insets(5, 5, 5, 10));
        options.setAlignment(Pos.TOP_LEFT);
        options.getChildren().addAll(new Text("Iterations:"), inputIterations,
                new Text("Population size:"), inputPopulationSize,
                new Text("Mutation rate:"), inputMutationRate,
                new Text("Number of tournaments:"),inputNumberOfTournaments,
                masterIslandCheckBox,
                new Text("Number of islands:"), inputNumberOfIslands,
                new Text("Migration frequency:"), inputMigrationFrequency,
                new Text("Migration rate:"), inputMigrationRate,
                new Text("Elitist island integration:"), islandIntegrationCount,
                new Text("Name of dataset:"),dataSetBox,
                new Text("Dataset split:"),inputDataSetSplit,
                new Text("Validation split:"),inputValidationSplit,
                new Text("k-fold cross validation:"),inputK,
                new Text("Radius multiplier:"), radiusMultiplier,
                new Text("PCA projection"),pca,
                radiusCheckBox,
                plotSolutionCheckBox,
                globalSharingFactorCheckBox);
        setLeft(options);

        startButton.setOnAction((e) -> {
            AISIGA.run(Integer.valueOf(inputIterations.getText()),
                    Integer.valueOf(inputPopulationSize.getText()),
                    Double.valueOf(inputMutationRate.getText()),
                    Integer.valueOf(inputNumberOfTournaments.getText()),
                    dataSetBox.getValue(),
                    labelIndex,
                    Double.valueOf(inputDataSetSplit.getText()),
                    Double.valueOf(inputMigrationFrequency.getText()),
                    Integer.valueOf(inputNumberOfIslands.getText()),
                    Double.valueOf(inputMigrationRate.getText()),
                    masterIslandCheckBox.isSelected(),
                    Integer.valueOf(inputK.getText()),
                    Integer.valueOf(islandIntegrationCount.getText()),
                    Integer.valueOf(pca.getText()),
                    radiusCheckBox.isSelected(),
                    Double.valueOf(inputValidationSplit.getText()),
                    Double.valueOf(radiusMultiplier.getText()),
                    plotSolutionCheckBox.isSelected(),
                    globalSharingFactorCheckBox.isSelected());
        });
        stopButton.setOnAction(event -> AISIGA.stopRunning());
        stopButton.setDisable(true);
        primaryStage.show();
    }

    public void setAntibodyGenerations(ArrayList<HashMap<String,ArrayList<Antibody>>> antibodyGenerations[], int[] bestIterations, ArrayList<Double> antibodyGenerationAccuracies[],HashMap<String,ArrayList<Antigen>> antigenMap, HashMap<String,ArrayList<Antigen>> antigenTestMap, boolean radiusPlot){
        //this.antibodyGenerations = antibodyGenerations;

        setBox = new ChoiceBox<>();
        setBox.getItems().setAll(islandList(antibodyGenerations.length));
        HBox menu2 = new HBox(5);
        menu2.setAlignment(Pos.CENTER);
        menu2.getChildren().setAll(new Text("View iteration 0-"+(antibodyGenerations[antibodyGenerations.length-1].size()-1)+":"),iterationTextField,new Text("at island:"),setBox);
        menuWrapper.getChildren().setAll(menu,menu2);
        setBox.getSelectionModel().selectLast();

        setBox.setOnAction((e) -> {
            setBestAccuracyIteration(antibodyGenerationAccuracies[Integer.parseInt(setBox.getValue())-1].get(bestIterations[Integer.parseInt(setBox.getValue())-1]), bestIterations[Integer.parseInt(setBox.getValue())-1]);
            boolean isInteger = tryParseInt(iterationTextField.getText());
            if(!isInteger){
                this.drawSolution(antigenTestMap,antibodyGenerations[Integer.parseInt(setBox.getValue())-1].get(bestIterations[antibodyGenerations.length-1]),0.0,radiusPlot);
            }else{
                int iteration = Integer.valueOf(iterationTextField.getText());
                int islandNumber = Integer.parseInt(setBox.getValue())-1;
                if(iteration >= 0 && iteration <antibodyGenerations[islandNumber].size()){
                    HashMap<String,ArrayList<Antibody>> antibodyMap = antibodyGenerations[islandNumber].get(iteration);
                    double accuracy = antibodyGenerationAccuracies[islandNumber].get(iteration);
                    this.drawSolution(antigenMap,antibodyMap,accuracy,radiusPlot);
                }
            }
        });

        //setBestAccuracyIteration(ais.getBestAccuracy(), ais.getBestIteration());
        iterationTextField.setText("Test");
        iterationTextField.setOnKeyPressed((e) ->{
            boolean isInteger = tryParseInt(iterationTextField.getText());
            if(e.getCode() == KeyCode.ENTER){
                if(!isInteger){
                    this.drawSolution(antigenTestMap,antibodyGenerations[Integer.parseInt(setBox.getValue())-1].get(bestIterations[antibodyGenerations.length-1]),0.0,radiusPlot);
                }else{
                    int iteration = Integer.valueOf(iterationTextField.getText());
                    int islandNumber = Integer.parseInt(setBox.getValue())-1;
                    if(iteration >= 0 && iteration <antibodyGenerations[islandNumber].size()){
                        HashMap<String,ArrayList<Antibody>> antibodyMap = antibodyGenerations[islandNumber].get(iteration);
                        double accuracy = antibodyGenerationAccuracies[islandNumber].get(iteration);
                        this.drawSolution(antigenMap,antibodyMap,accuracy,radiusPlot);
                    }
                }
            }
            else if(e.getCode() == KeyCode.DOWN && isInteger){
                int iteration = Integer.valueOf(iterationTextField.getText())-1;
                int islandNumber = Integer.parseInt(setBox.getValue())-1;
                if(iteration >= 0 && iteration <antibodyGenerations[islandNumber].size()){
                    HashMap<String,ArrayList<Antibody>> antibodyMap = antibodyGenerations[islandNumber].get(iteration);
                    double accuracy = antibodyGenerationAccuracies[islandNumber].get(iteration);
                    this.drawSolution(antigenMap,antibodyMap,accuracy,radiusPlot);
                    iterationTextField.setText(Integer.toString(iteration));
                }
            }else if(e.getCode() == KeyCode.DOWN){
                int islandNumber = Integer.parseInt(setBox.getValue())-1;
                int iteration = antibodyGenerations[islandNumber].size()-1;
                HashMap<String,ArrayList<Antibody>> antibodyMap = antibodyGenerations[islandNumber].get(iteration);
                double accuracy = antibodyGenerationAccuracies[islandNumber].get(iteration);
                this.drawSolution(antigenMap,antibodyMap,accuracy,radiusPlot);
                iterationTextField.setText(Integer.toString(iteration));
            }
            else if(e.getCode() == KeyCode.UP && isInteger){
                int iteration = Integer.valueOf(iterationTextField.getText())+1;
                int islandNumber = Integer.parseInt(setBox.getValue())-1;
                if(iteration >= 0 && iteration < antibodyGenerations[islandNumber].size()){
                    HashMap<String,ArrayList<Antibody>> antibodyMap = antibodyGenerations[islandNumber].get(iteration);
                    double accuracy = antibodyGenerationAccuracies[islandNumber].get(iteration);
                    this.drawSolution(antigenMap,antibodyMap,accuracy,radiusPlot);
                    iterationTextField.setText(Integer.toString(iteration));
                }else if(iteration == antibodyGenerations[islandNumber].size()){
                    iterationTextField.setText("Test");
                    this.drawSolution(antigenTestMap,antibodyGenerations[Integer.parseInt(setBox.getValue())-1].get(bestIterations[antibodyGenerations.length-1]),0.0,radiusPlot);
                }
            }
        });
    }
    public String[] islandList(int islandCount) {
        String[] islandCountArray = new String[islandCount];
        for(int i=0;i<islandCount;i++){
            islandCountArray[i] = i+1+"";
        }
        return islandCountArray;
    }

        private boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    public void createSolutionGraph(HashMap<String, double[][]> featureMap, HashMap<String,ArrayList<Antibody>> antibodyMap) {
        this.solutionGraph = new SolutionGraph(solutionGraphWidth, solutionGraphHeight, featureMap,antibodyMap);
        setCenter(solutionGraph);
    }

    public void drawSolution(HashMap<String, ArrayList<Antigen>> antigenMap, HashMap<String, ArrayList<Antibody>> antibodyMap, double accuracy, boolean radiusPlot){
        solutionGraph.drawSolutionGraph(antigenMap,antibodyMap,accuracy, radiusPlot);
    }

    public void setAccuracy(double accuracy) {
        solutionGraph.setAccuracy(accuracy);
    }

    public void createStatisticGraph(int iterations,int graphCount, boolean master) {
        graphs = new ArrayList<>();
        graphPane.setAlignment(Pos.TOP_CENTER);
        graphPane.setVgap(50);
        graphPane.setHgap(50);
        graphPane.setPrefWrapLength(1200);
        //setCenter(graphPane);
        if(graphCount ==1){
            graphs.add(new StatisticGraph(statisticGraphWidth, statisticGraphHeight, iterations,true,true,0));
        }else{
            for(int i=0;i <graphCount;i++){
                StatisticGraph graph;
                if(master && i == graphCount-1){
                    graph = new StatisticGraph(550, 300, iterations,false,false,i);
                }else{
                    graph = new StatisticGraph(550, 300, iterations,false,true,i);
                }
                //graph.setPadding(new Insets(100, 0, 100, 100));
                graphs.add(graph);
            }
        }
        //StatisticGraph statisticGraph2 = new StatisticGraph(statisticGraphWidth, statisticGraphHeight, iterations);
        //primaryStage.setHeight(700);
        graphPane.getChildren().clear();
        graphPane.getChildren().addAll(graphs);
        graphPane.setPadding(new Insets(100, 0, 100, 100));
        //graphPane.setMinWidth(1100);
        scrollPane.setContent(graphPane);
        //scrollPane.setPadding(new Insets(100, 100, 200, 200));
        setCenter(scrollPane);
        //scrollPane.setMinWidth(1100);

        /*scrollPane.setContent(graphPane);
        setCenter(scrollPane);
        scrollPane.setPadding(new Insets(100, 100, 100, 100));*/
        //setCenter(statisticGraph);
    }

    public void addIteration(double fitness, boolean migration, int graphIndex) {
        graphs.get(graphIndex).addIteration(fitness,migration);
        //statisticGraph.addIteration(fitness, migration);
    }

    public void setBestAccuracy(double accuracy,int graphIndex) {
        graphs.get(graphIndex).setBestAccuracy(accuracy);
    }
    public void setAverageAccuracy(double accuracy,int graphIndex) {
        graphs.get(graphIndex).setAverageAccuracy(accuracy);
    }

    public void setBestAccuracyIteration(double accuracy, int iteration) {
        solutionGraph.setBestAccuracyIteration(accuracy, iteration);
    }

}
