import AIS.AIS;
import AIS.Antigen;
import AIS.Antibody;
import GUI.Graph;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Main extends Application {


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane borderPane = new BorderPane();
        Scene scene = new Scene(borderPane, 1200, 800);
        primaryStage.setScene(scene);
        primaryStage.setTitle("legendary-octo-sniffle");


        DataSet dataSet = new DataSet("./DataSets/iris.data",0.1);

        AIS ais = new AIS(dataSet.trainingSet,dataSet.antigenMap,1000, 0.1, 5);
        for(int i=0;i<10;i++){
            ais.iterate();
        }
        ais.vote(dataSet.testSet);
        /*for(Antigen antigen:dataSet.antigens){
            System.out.println(antigen);
        }*/

        //creating hasmap for testset
        HashMap<String,ArrayList<Antigen>> testSetMap = new HashMap<>();
        for(Antigen antigen: dataSet.testSet){
            if(!testSetMap.containsKey(antigen.getLabel())){
                testSetMap.put(antigen.getLabel(),new ArrayList<>(){{add(antigen);}});
            }else{
                testSetMap.get(antigen.getLabel()).add(antigen);
            }
        }

        Graph graph = new Graph(400, 500, ais.getFeatureMap(), ais.getAntibodyMap());
        BorderPane.setAlignment(graph, Pos.CENTER);
        graph.setAntigens(testSetMap);
        graph.setAntibodies(ais.getAntibodyMap());
//        graph.setAntibodies(ais.getAntibodies());
        borderPane.setCenter(graph);

        primaryStage.show();
    }

}
