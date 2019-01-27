import AIS.AIS;
import AIS.Antigen;
import AIS.Antibody;
import GUI.Graph;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main extends Application {


    //private TextField populationSizeInput = new TextField();
    private static int POPULATION_SIZE = 1000;
    //TextField populationSizeInput = new TextField();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("legendary-octo-sniffle");
        VBox optionMenu = new VBox(5);
        optionMenu.setTranslateX(5);
        optionMenu.setAlignment(Pos.CENTER);
        //TextField populationSizeInput = new TextField();
        //populationSizeInput.setText(String.valueOf(POPULATION_SIZE));
        //optionMenu.getChildren().addAll(new Text("Initial Population Size"), populationSizeInput);
        borderPane.setLeft(optionMenu);

        DataSet dataSet = new DataSet("./DataSets/iris.data",0.1);

        AIS ais = new AIS(dataSet.trainingSet,dataSet.antigenMap,100, 0.8, 5);

        Graph graph = new Graph(400, 400, ais.getFeatureMap());
        BorderPane.setAlignment(graph, Pos.CENTER);
        //graph.setAntigens(ais.getAntigenMap());
        graph.setAntigens(ais.getAntigenMap());
        //graph.setAntibodies(ais.getAntibodyMap());
        //graph.setConnections();
        borderPane.setCenter(graph);
        primaryStage.show();
        ArrayList<HashMap<String,ArrayList<Antibody>>> antibodyGenerations = new ArrayList<>();

        for(int i=0;i<10;i++){
            antibodyGenerations.add(AIS.copy(ais.getAntibodyMap()));
            ais.iterate();
        }

        ais.vote(dataSet.testSet);

        //creating hashmap for testset
        HashMap<String,ArrayList<Antigen>> testSetMap = new HashMap<>();
        for(Antigen antigen: dataSet.testSet){
            if(!testSetMap.containsKey(antigen.getLabel())){
                testSetMap.put(antigen.getLabel(),new ArrayList<>(){{add(antigen);}});
            }else{
                testSetMap.get(antigen.getLabel()).add(antigen);
            }
        }

        //graph.setAntigens(testSetMap);
        graph.setAntigens(ais.getAntigenMap());
        /*for (HashMap<String,ArrayList<Antibody>> map: antibodyGenerations){
            graph.setAntibodies(antibodyGenerations.get(0));
            graph.setConnections();
        }*/
        graph.setAntibodies(antibodyGenerations.get(9));
        graph.setConnections();

    }

    /*private int getPopulationSize() {
        return Integer.valueOf(populationSizeInput.getText());
    }*/
}
