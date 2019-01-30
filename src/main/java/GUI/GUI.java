package GUI;

import AIS.Antigen;
import AIS.Antibody;
import Algorithm.LegendaryOctoSniffle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;

public class GUI extends BorderPane {
    private final Stage primaryStage;
    private final LegendaryOctoSniffle LOS;
    private Graph graph;
    public final Button startButton = new Button("Start");
    public final Button stopButton = new Button("Stop");

    private final TextField inputIterations = new TextField("10");
    private final TextField inputPopulationSize = new TextField("100");
    private final TextField inputMutationRate = new TextField("0.8");
    private final TextField inputNumberOfTournaments = new TextField("5");
    private final TextField inputDataSetName = new TextField("iris.data");
    private final TextField inputDataSetSplit = new TextField("0.1");
    private HBox menu;
    private ArrayList<HashMap<String,ArrayList<Antibody>>> antibodyGenerations;

    private ChoiceBox<String> taskBox;

    public GUI(Stage primaryStage, LegendaryOctoSniffle LOS){
        super();
        this.antibodyGenerations = null;
        this.primaryStage = primaryStage;
        this.LOS = LOS;
        final Scene scene = new Scene(this, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("legendary-octo-sniffle");

        primaryStage.show();

        menu = new HBox(5);
        menu.setPadding(new Insets(5,0,0,0));
        menu.setAlignment(Pos.CENTER);
        menu.getChildren().addAll(startButton, stopButton);
        setTop(menu);

        VBox options = new VBox(10);
        options.setPadding(new Insets(5, 5, 5, 10));
        options.setAlignment(Pos.CENTER);
        options.getChildren().addAll(new Text("Iterations:"), inputIterations,
                new Text("Population size:"), inputPopulationSize,
                new Text("Mutation rate:"), inputMutationRate,
                new Text("Number of tournaments:"),inputNumberOfTournaments,
                new Text("Name of dataset:"),inputDataSetName,
                new Text("Dataset split:"),inputDataSetSplit);
        setLeft(options);

        startButton.setOnAction((e) -> {
            LOS.run(Integer.valueOf(inputIterations.getText()),Integer.valueOf(inputPopulationSize.getText()),Double.valueOf(inputMutationRate.getText()),Integer.valueOf(inputNumberOfTournaments.getText()),inputDataSetName.getText(),Double.valueOf(inputDataSetSplit.getText()));
        });
        stopButton.setOnAction(event -> LOS.stopRunning());

        stopButton.setDisable(true);
        primaryStage.show();
    }

    public void setAntibodyGenerations(ArrayList<HashMap<String,ArrayList<Antibody>>> antibodyGenerations, HashMap<String,ArrayList<Antigen>> antigenMap, HashMap<String,ArrayList<Antigen>> antigenTestMap, HashMap<String,ArrayList<Antibody>> antibodyTestMap){
        this.antibodyGenerations = antibodyGenerations;
        this.taskBox = new ChoiceBox<>();
        this.taskBox.getItems().setAll(iterationList(antibodyGenerations.size()));
        menu.getChildren().setAll(startButton, stopButton, taskBox);

        taskBox.setOnAction((e) -> {
            if(taskBox.getValue().equals("Test")){
                //HashMap<String,ArrayList<Antibody>> antibodyMap = antibodyGenerations.get(antibodyGenerations.size()-1);
                graph.setAntigens(antigenTestMap);
                graph.setAntibodies(antibodyTestMap);
                graph.setConnections();
            }else{
            HashMap<String,ArrayList<Antibody>> antibodyMap = antibodyGenerations.get(Integer.valueOf(taskBox.getValue())-1);
            graph.setAntigens(antigenMap);
            graph.setAntibodies(antibodyMap);
            graph.setConnections();
        }
        });
    }

    public String[] iterationList(int iterations){
        String[] iterationsArray = new String[iterations+1];
        for(int i=0;i<iterations;i++){
            iterationsArray[i] = i+1+"";
        }
        iterationsArray[iterations] = "Test";
        return iterationsArray;
    }
     public void createSolutionGraph(HashMap<String, double[][]> featureMap) {
        this.graph = new Graph(400, 400, featureMap);
        setCenter(graph);
        BorderPane.setAlignment(graph, Pos.CENTER);
     }

    public void drawSolution(HashMap<String, ArrayList<Antigen>> antigenMap, HashMap<String, ArrayList<Antibody>> antibodyMap){
        graph.setAntigens(antigenMap);
        graph.setAntibodies(antibodyMap);
        graph.setConnections();
    }

    public void setAccuracy(double accuracy) {
        graph.setAccuracy(accuracy);
    }
}
